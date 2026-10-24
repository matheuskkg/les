# DEC-15.3 — Saldo de estoque

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-23
- **Card:** #72
- **Requisitos:** RF-EST-002, RF-EST-003, RF-EST-004, RN-EST-002, RN-EST-006, RN-VEN-002, RN-VEN-014

## Contexto

O `arquitetura.md` trava "a linha de estoque" com lock pessimista ao reservar e ao baixar, mas não diz se o saldo é
gravado ou calculado a partir das entradas, nem como o seed o mantém coerente com elas.

## Decisão

O saldo fica numa linha por livro. Como não há variações nem vários locais, o livro é o SKU: cada edição é outro
livro, com outro ISBN.

| Tabela | Papel |
|---|---|
| `estoque` | `livro_id` (único) e `quantidade` em mãos. É a linha travada com lock pessimista |
| `entrada_estoque` | histórico das entradas: livro, quantidade, custo, fornecedor e data (RN-EST-001). Dá o maior custo para o preço de venda (RN-EST-002) |
| `reserva` | itens bloqueados no carrinho ou no checkout, com prazo |

- **Disponível** = `quantidade` − reservas ativas do livro, calculado na consulta. A linha não guarda contador de
  reservado.
- **Reservar:** trava a linha do livro e confere o disponível antes de gravar a reserva.
- **Baixa (RF-EST-003):** na aprovação, trava a linha, subtrai a quantidade e apaga as reservas do checkout.
- **Reentrada (RF-EST-004):** soma a quantidade do item da troca que volta ao estoque. Não cria entrada, porque não
  há custo novo; o histórico fica na auditoria do Envers.
- **Seed:** o script insere as entradas e cria o saldo a partir delas, com SQL que roda igual em H2 e Postgres:

  ```sql
  INSERT INTO estoque (id, livro_id, quantidade)
  SELECT livro_id, livro_id, SUM(quantidade) FROM entrada_estoque GROUP BY livro_id;
  ```

  O id do saldo é o do livro, dentro da faixa do seed ([DEC-14](DEC-14-ids-nomes-e-seed.md)).

## Alternativas consideradas

- Saldo calculado pela soma das entradas menos as baixas: não há linha para travar, e a soma cresce com o histórico.
- Quantidades em mãos, comprometida e disponível na mesma linha, como na Shopify: a reserva já é uma linha com
  prazo, e um contador de reservado obrigaria a expiração a mantê-lo em dia.
- Saldo por local: a loja tem um só.

## Consequências

- O módulo `estoque` expõe reservar, liberar, baixar e reentrar como casos de uso chamados por `vendas`
  ([DEC-13](DEC-13-processamento-do-pedido-e-notificacoes.md)).
- O seed `0NN` insere `entrada_estoque` e deriva `estoque` delas; o saldo nunca é digitado à mão.
