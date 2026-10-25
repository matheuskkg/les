# DEC-16 — Modelo de dados

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-23
- **Card:** #73
- **Requisitos:** RF-VEN-001, RF-VEN-005, RF-VEN-006, RF-VEN-008, RNF-VEN-001, RN-VEN-014, RN-CLI-005, RF-ANA-001,
  RN-ANA-001

## Contexto

O MER (`docs/modelo/`) reúne as tabelas que os DECs anteriores definem, mas alguns pontos ficaram sem decisão:

- como o pedido guarda as cópias do [DEC-12](DEC-12-pedido-checkout-e-trocas.md);
- como uma tabela referencia a de outro módulo, se os módulos não acessam a persistência um do outro
  (`arquitetura.md`);
- a quem pertence uma reserva, já que o `estoque` não conhece carrinho nem checkout;
- onde ficam os itens removidos do carrinho por expiração (RNF-VEN-001);
- se o checkout guarda endereços, cartões e cupons entre as tentativas de pagamento;
- em que módulo fica a bandeira do cartão.

## Decisão

### Modelo

O MER conceitual e o lógico ficam em `docs/modelo/mer.drawio`, com uma página para cada um e os PNGs exportados. O
arquivo é gerado por `docs/modelo/gerar_mer.py` a partir de `mer.spec.json` (lógico) e `conceitual.spec.json`
(conceitual). Uma mudança no modelo é feita nos specs, e o diagrama é gerado de novo.

### Snapshots do pedido em JSON

As cópias do pedido ([DEC-12](DEC-12-pedido-checkout-e-trocas.md)) são **snapshots imutáveis**: o objeto como era no
momento da compra, numa coluna JSON (`@JdbcTypeCode(SqlTypes.JSON)`, que o Hibernate mapeia em H2 e Postgres). Todo
snapshot traz o **id de origem**.

| Tabela | Coluna JSON | Conteúdo |
|---|---|---|
| `pedido` | `cliente` | id, nome, CPF, e-mail e telefone |
| `pedido` | `endereco_entrega`, `endereco_cobranca` | id e campos do endereço |
| `item_pedido` | `livro` | id, título, autores, editora, edição, ISBN e categorias (`[{id, nome}]`) |
| `pagamento_cartao` | `cartao` | id (se salvo no perfil), bandeira e 4 últimos dígitos |
| `cupom_pedido` | `cupom` | id, código, categoria e tipo |

- Fatos da compra continuam em colunas: preço unitário, quantidades, valores, status, datas e as chaves do próprio
  pedido. `pedido.cliente_id` também é coluna, porque filtra "meus pedidos", a consulta do admin e o ranking.
- Por isso não há `item_pedido_categoria`, `item_pedido.livro_id` nem `cupom_pedido.cupom_id`: esses ids ficam no
  snapshot.
- **Análise (RN-ANA-001):** a consulta busca os itens dos pedidos do período e agrega por categoria em Java, lendo as
  categorias do snapshot do livro. O SQL de JSON difere entre H2 e Postgres.
- **Reprovação:** a devolução do cupom de troca ([DEC-11](DEC-11-cupons-e-distribuicao-do-pagamento.md)) usa o id do
  snapshot em `cupom_pedido.cupom`.

### Referências entre módulos

Uma coluna que aponta para a tabela de outro módulo (`cliente.usuario_id`, `item_carrinho.livro_id`,
`notificacao.usuario_id`...) é só o id: o `*Jpa` não tem `@ManyToOne` para a entidade do outro módulo, e o banco não
tem a restrição de FK. Dentro do módulo, as referências são FKs normais.

### Reserva

- `reserva`: `livro_id`, `quantidade`, `expira_em`, `origem` (`CARRINHO` ou `CHECKOUT`) e `origem_id`.
- O `estoque` trata `origem` e `origem_id` como uma referência opaca, como o `recurso` da notificação
  ([DEC-13](DEC-13-processamento-do-pedido-e-notificacoes.md)). Passar as reservas do carrinho ao checkout é um
  `UPDATE` de `origem`, `origem_id` e `expira_em`.
- Reserva ativa é a que tem `expira_em` no futuro. O disponível ([DEC-15.3](DEC-15.3-saldo-de-estoque.md)) fica certo
  mesmo antes de a tarefa de expiração rodar.
- O carrinho e o checkout guardam o próprio `expira_em` e o `aviso_enviado_em`, usados pelo aviso de expiração.

### Itens removidos do carrinho (RNF-VEN-001)

- Tabela `item_removido_carrinho`: carrinho, livro, quantidade e `removido_em`, com `UNIQUE (carrinho_id, livro_id)`.
- A expiração do carrinho ou do checkout grava os itens nela. A linha some quando o cliente adiciona o livro de novo
  ou dispensa o aviso.
- `item_carrinho` e `item_checkout` também têm `UNIQUE` por livro: os itens que voltam do checkout são somados à linha
  existente ([DEC-12](DEC-12-pedido-checkout-e-trocas.md)).

### Checkout

O checkout não guarda endereços, cartões nem cupons. Cada finalização os recebe na requisição, junto com o CVV
([DEC-10](DEC-10-cliente-enderecos-e-cartoes.md)), e uma nova tentativa os envia de novo.

### Bandeira

A tabela `bandeira` fica no módulo `clientes`, o único que a usa (RN-CLI-005), e `cartao.bandeira_id` é FK. O seed
dela é do `2NN` ([DEC-14](DEC-14-ids-nomes-e-seed.md)) (alterado pelo
[DEC-18](DEC-18-bandeira-identificada-pelo-numero.md): a bandeira é identificada pelo número, e a tabela ganha as
colunas `prefixos` e `tamanhos`).

## Alternativas consideradas

- Cópias em colunas (`cliente_nome`, `entrega_cep`...): muitas colunas no pedido, e a cópia de listas (categorias,
  autores) exige outra tabela.
- `item_pedido_categoria` para a análise filtrar em SQL: uma tabela só para um índice de um volume pequeno.
- FK entre módulos: o `*Jpa` de um módulo dependeria da persistência do outro.
- FK da reserva para `item_carrinho`/`item_checkout`: o `estoque` dependeria das tabelas do `vendas`.
- Reserva sem dono, só por cliente: a inativação e a expiração não saberiam se a reserva é do carrinho ou do checkout.
- Itens removidos como situação no `item_carrinho`: mistura itens ativos e histórico em toda consulta do carrinho.
- Checkout guardando a seleção de pagamento: o CVV não é gravado, então a tentativa seguinte pede os cartões de
  qualquer jeito.
- Bandeira no `catalogo`: ninguém do catálogo a usa, e `cartao.bandeira_id` seria referência entre módulos.

## Consequências

- O `vendas` monta os snapshots na finalização (`RegistrarPedido`), e os DTOs do pedido os expõem como objetos.
- A consulta da análise lê as categorias do snapshot do livro; os testes cobrem itens com várias categorias.
- `bandeira` sai do INF-05 e entra no CLI-01, com o seed no `2NN`.
- O `*Jpa` de cada módulo só conhece as entidades do próprio módulo.
