# DEC-14 — IDs, nomes e seed

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-23
- **Card:** #67
- **Requisitos:** RNF-LIV-001, RNF-LIV-002, RNF-CLI-005, RNF-GER-001, RNF-GER-002

## Contexto

O DRS pede código único para livro (RNF-LIV-001) e cliente (RNF-CLI-005), e um script de implantação com os
domínios (RNF-LIV-002). Sem migrations, o schema vem do Hibernate (`ddl-auto`), e os scripts de seed precisam rodar
iguais em H2 e Postgres (`arquitetura.md`). Falta definir:

- o tipo e a geração dos IDs;
- como o seed usa IDs fixos sem colidir com os gerados;
- os nomes de tabelas e colunas e onde ficam os índices;
- como dividir o seed entre os cards;
- como inserir os pedidos históricos.

## Decisão

### IDs

- Toda entidade tem ID `Long`, gerado por **`SEQUENCE` própria da tabela** (`<tabela>_seq`), com valor inicial
  **10000**. A faixa de 1 a 9999 fica reservada ao seed, que usa IDs explícitos.
  - Uma sequence com valor inicial fixo é criada pelo Hibernate da mesma forma nos dois bancos.
  - O seed não precisa ajustar sequences: `setval` e o reinício de identity têm sintaxe diferente em H2 e Postgres.
  - O `POST /api/teste/reset` ([DEC-09](DEC-09-topologia-de-execucao.md)) limpa as tabelas e reinsere o seed com os
    mesmos IDs, sem tocar nas sequences.
- O ID é o **código exibido**: código do cliente (RNF-CLI-005, [DEC-10](DEC-10-cliente-enderecos-e-cartoes.md)),
  código do livro (RNF-LIV-001) e número do pedido. O ISBN é um atributo do livro, não o código.
- O código do cliente é exibido, mas não é filtro da consulta do admin.

Fundamento:

- UUID v4 fragmenta o índice da chave primária.
- UUID v7 resolve isso, mas só compensa quando o ID é gerado fora do banco ou não pode ser adivinhado. Aqui o banco
  é único, e toda rota é autorizada pelo usuário logado ([DEC-07](DEC-07-convencoes-da-api-rest.md)).
- A sequence dá códigos legíveis, índice menor e inserção em lote pelo Hibernate, o que `IDENTITY` impede.
- IDs crescentes também servem ao `Last-Event-ID` do SSE ([DEC-13](DEC-13-processamento-do-pedido-e-notificacoes.md)).

### Nomes

- Tabelas e colunas em **snake_case e em português**, no singular (`cliente`, `item_pedido`, `livro_categoria`).
- Cada entidade `*Jpa` declara `@Table(name = ...)`. Sem isso, a estratégia de nomes do Spring geraria
  `cliente_jpa`.
- As tabelas do Envers têm o sufixo `_aud` (`org.hibernate.envers.audit_table_suffix`).

### Índices (RNF-GER-001)

- Declarados em `@Table(indexes = @Index(...))` nas entidades.
- Cobrem as colunas filtradas ou ordenadas nas consultas ([DEC-07](DEC-07-convencoes-da-api-rest.md)) e as chaves
  estrangeiras usadas em consultas, porque o Postgres não indexa FK sozinho.
- Uma mudança de índice num banco Postgres existente exige recriá-lo, como qualquer mudança incompatível do schema.

### Divisão do seed

Uma tabela só existe depois que a entidade correspondente foi criada. Por isso cada script entra no card que cria
as entidades dele. A faixa do nome define a ordem de execução:

| Faixa | Conteúdo | Card |
|---|---|---|
| `0NN` | domínios (autor, editora, categoria, grupo de precificação, fornecedor, bandeira (alterado pelo [DEC-16](DEC-16-modelo-de-dados.md): bandeira no `2NN`)), livros e entradas de estoque | INF-05 |
| `1NN` | administrador e usuários | INF-06 |
| `2NN` | clientes, com endereços e cartões | CLI-01 |
| `3NN` | cupons promocionais e cupons de troca | VEN-06 |
| `4NN` | pedidos históricos e trocas | ANA-01 |

### Pedidos históricos

- Inseridos **diretamente por SQL**, com datas fixas no passado e status finais coerentes com o
  [DEC-12](DEC-12-pedido-checkout-e-trocas.md): a maioria `ENTREGUE`, alguns com troca parcial ou total e alguns
  `REPROVADA`. As cópias do pedido são preenchidas como a finalização faria.
- Volume: cerca de 24 meses, de novembro de 2024 a outubro de 2026, em 4 a 5 categorias, com algumas centenas de
  pedidos, o suficiente para linhas distintas no gráfico (RNF-ANA-001).
- O script é versionado já pronto; um gerador, se usado, fica fora da aplicação.
- Esses registros não têm revisão no Envers. Para dados de seed, isso é aceito.

## Alternativas consideradas

- `IDENTITY`: impede a inserção em lote, e o seed teria de referenciar tudo por subselect (`SELECT id FROM ...`).
- UUID v7 em tudo: códigos exibidos longos e índice maior, sem precisar de IDs gerados fora do banco.
- UUID nas entidades internas e `Long` nas exibidas: duas convenções sem ganho.
- ISBN como código do livro: um segundo identificador sem necessidade.
- Valor inicial 1000: a faixa do seed não comportaria os itens dos pedidos históricos.
- Todo o seed no INF-05: o card esperaria as entidades de todos os módulos.
- Pedidos históricos pelos casos de uso, com o `Relogio` fixo no passado: passaria pelas regras e pela auditoria,
  mas exigiria um seeder em Java e deixaria o reset dos E2E mais lento.
- Datas relativas a `CURRENT_DATE`: a aritmética de intervalos difere entre H2 e Postgres, e o gráfico deixaria de
  ser determinístico.

## Consequências

- Cada `*Jpa` declara `@Table`, `@SequenceGenerator(initialValue = 10000)` e os índices.
- O código do cliente deixa de ser filtro da consulta do admin (DEC-10).
- Como as datas do histórico são fixas, testes do ranking e da análise fixam o relógio dentro do período do seed.
- As pendências do seed ficam nos cards da tabela acima.
