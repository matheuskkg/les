# DEC-21 — Ampliação do catálogo e do volume de vendas do seed

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-11-18
- **Card:** ALT-02
- **Requisitos:** RNF-LIV-002, RN-LIV-001, RF-EST-002, RN-EST-002, RF-ANA-001, RNF-REC-001
- **Substitui:** apenas o volume de pedidos e a densidade do [DEC-20](DEC-20-janela-e-densidade-do-seed.md). A janela
  de 36 meses (nov/2023 a out/2026), os IDs de 1 a 9999, as sequences a partir de 10000, a divisão dos scripts por
  faixa e a preservação dos pedidos já conferidos permanecem em vigor.

## Contexto

O catálogo do seed tinha 26 livros, em 5 categorias, 14 autores e 7 editoras. O histórico tinha 847 pedidos em 36
meses. Para a apresentação, as recomendações e o chatbot (RNF-REC-001), esse volume limita o número de cenários e de
interações possíveis. O DRS não fixa tamanho de catálogo nem volume de vendas; o DEC-20 fixou números que podem ser
ampliados sem mudar regra de negócio, contrato de API ou janela da análise — a RN-ANA-002 limita o recorte consultado,
não o histórico armazenado.

## Decisão

### Catálogo

Três scripts novos na faixa `0NN`, executados em ordem pelo `ExecutorDeSeeds` depois dos scripts originais:

- `010_dominios_ampliados.sql`: 15 autores (15 a 29), 8 editoras (8 a 15), 12 categorias (6 a 17), 2 grupos de
  precificação (4 `Econômico`, 15%; 5 `Colecionador`, 70%) e 5 fornecedores (6 a 10).
- `011_livros_ampliados.sql`: 30 livros (27 a 56), com todos os campos obrigatórios de RN-LIV-001, ISBN-13 e EAN-13
  válidos e únicos, e associações de autores e categorias. Nenhum título, autor ou editora existente é alterado.
- `012_estoque_ampliado.sql`: 34 entradas (40 a 73) e as linhas de saldo dos livros 27 a 56. Os livros 37, 38, 47 e
  50 têm duas entradas com custos distintos, para exercitar a RN-EST-002. O preço segue sendo calculado como maior
  custo × (1 + margem do grupo), arredondado só no fim (RF-EST-002, DEC-15.1); nada é gravado fixo no seed.

Totais: 56 livros, 73 entradas, 29 autores, 15 editoras, 17 categorias, 5 grupos e 10 fornecedores.

### Vendas

O gerador `apps/server/scripts/gerar_seed_historico.py` ganha um quarto laço, com semente própria (`2031`), que
acrescenta 333 pedidos (ids 848 a 1180), 614 itens, 49 trocas e 20 cupons de troca em nov/2024 a set/2026, exceto
mar/2025. Os status incluem `ENTREGUE`, `APROVADA`, `EM_TRANSPORTE` e `REPROVADA`; as trocas incluem `TROCADO`,
`TROCA_AUTORIZADA` e `TROCA_RECUSADA`, com reentrada de estoque e cupons de troca coerentes ([DEC-12](DEC-12-pedido-checkout-e-trocas.md)).

Totais: 1180 pedidos, 2138 itens, 147 trocas, 83 cupons de troca e 36 meses com vendas.

### Preservação

Os três laços anteriores continuam a sortear exatamente os livros 1 a 18 (`LIVROS_ORIGINAIS`): acrescentar chaves ao
dicionário `LIVROS` sem essa separação deslocaria o `random.sample` e mudaria pedidos, datas e valores conferidos.

Os 847 pedidos originais mantêm ids, valores, datas e status. Os clientes da Carla (3) e da Elisa (5) não recebem
pedidos novos. O laço novo usa só os clientes 6 a 10 e só os 19 livros novos cujas categorias são todas novas (6 a
17). Assim, os totais das cinco categorias originais continuam exatamente os mesmos em março/2025, outubro/2026 e
nos demais meses conferidos por testes e pelo E2E de apresentação. A data mínima (nov/2023), a máxima (out/2026), a
janela estendida anterior a nov/2024 e o pedido 242 (`EM TRANSPORTE`) também ficam intactos.

## Alternativas consideradas

- **Editar os scripts originais de catálogo.** Mudar o checksum de arquivos já executados exigiria recriar bancos
  Postgres de desenvolvimento; os scripts `010` a `012` acrescentam dados sem tocá-los.
- **Incluir os livros novos no sorteio dos laços antigos.** Mudaria os valores conferidos pela análise e pelo E2E,
  além do histórico da Carla e da Elisa; por isso os laços antigos usam `LIVROS_ORIGINAIS`.
- **Usar todas as categorias nos pedidos novos.** Alteraria os totais das categorias 1 a 5 em meses conferidos valor
  a valor; restringir a massa nova às categorias 6 a 17 amplia a análise sem quebrar a apresentação.
- **Gerar pedidos em março/2025 ou outubro/2026.** São meses de referência dos testes de controller e do E2E; basta
  cobrir os outros 22 meses.
- **Registrar o preço de venda no seed.** Contradiz RF-EST-002/RN-EST-002 e a arquitetura ([DEC-15.3](DEC-15.3-saldo-de-estoque.md)).

## Consequências

- Nenhuma entidade, regra de domínio, endpoint ou contrato OpenAPI muda; não há regeneração de tipos do `web`.
- Os testes de seed, de vitrine, de preços e de análise passam a conferir as novas contagens e os novos domínios.
- As matrizes de RNF-LIV-002, RN-LIV-001, RN-EST-001, RF-ANA-001 e RNF-REC-001 registram os novos números.
- Bancos Postgres existentes precisam ser recriados para aplicar os scripts novos e o histórico regenerado; em H2 o
  schema e o seed são recriados a cada inicialização.
