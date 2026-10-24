# Cadastro de Livros

Prefixo do grupo: `LIV`. Fonte: DRS – E-commerce de Livros (LES 2026), v0.6.

## Requisitos funcionais

| ID | Nome | Descrição |
|---|---|---|
| RF-LIV-001 | Cadastrar livro | O sistema deve manter um cadastro único para livros. |
| RF-LIV-002 | Inativar cadastro de livro | O sistema deve possibilitar que livros sejam inativados. |
| RF-LIV-003 | Inativar livro de forma automática | O sistema deve inativar livros sem estoque e que não possuem venda com valor inferior a parâmetro predefinido no sistema. |
| RF-LIV-004 | Alterar cadastro de livro | O sistema deve possibilitar a alteração de dados cadastrais para os livros. |
| RF-LIV-005 | Consulta de livros | O sistema deve possibilitar que um livro seja consultado com base em um filtro definido pelo usuário. Todos os campos utilizados para identificação do livro podem ser utilizados como filtro, tanto de forma combinada como de forma isolada. |
| RF-LIV-006 | Ativar cadastro de livros | Deve ser possível ativar o cadastro de um livro. |

## Requisitos não funcionais

| ID | Nome | Descrição |
|---|---|---|
| RNF-LIV-001 | Código de livro | Todo livro cadastrado deve receber um código único no sistema. |
| RNF-LIV-002 | Cadastro de domínios | Deve haver um script de implantação do sistema que insere todos registros de tabelas de domínio necessárias, por ex.: grupo de precificação, autor, editora, fornecedor etc. |

## Regras de negócio

| ID | Nome | Descrição |
|---|---|---|
| RN-LIV-001 | Dados obrigatórios para o cadastro de um livro | Para todo livro cadastrado é obrigatório o cadastro dos seguintes dados: autor, categoria, ano, título, editora, edição, ISBN, número de páginas, sinopse, dimensões (altura, largura, peso e profundidade), grupo de precificação e código de barras. |
| RN-LIV-002 | Associação com categorias | Um livro pode estar associado com mais de uma categoria. |
| RN-LIV-003 | Definindo valor de venda | Todo livro após cadastrado deverá ser associado a um grupo de precificação onde o valor deverá ter como base a margem de lucro parametrizada para o grupo definido no cadastro do livro. |
| RN-LIV-004 | Validar margem de lucro | Um livro somente pode ter seu valor alterado se estiver dentro da margem de lucro definida pelo grupo de precificação. Para alterar o valor para baixo da margem é necessária autorização de um gerente de vendas. |
| RN-LIV-005 | Associar motivo de inativação | Todo livro que for inativado manualmente deve ter uma justificativa e uma categoria de inativação associada. |
| RN-LIV-006 | Associar motivo de inativação automática | Todo cadastro de livro inativado de forma automática deve ser categorizado como FORA DE MERCADO. |
| RN-LIV-007 | Associar motivo de ativação | Todo livro que for ativado deve ter uma justificativa e uma categoria de ativação associada. |

## Matriz de rastreabilidade

Status: ✅ implementado · 🟡 parcial · ❌ pendente.

| ID | Status | Onde no código | Observação |
|---|---|---|---|
| RF-LIV-001 | ❌ |  |  |
| RF-LIV-002 | ❌ |  |  |
| RF-LIV-003 | ❌ |  |  |
| RF-LIV-004 | ❌ |  |  |
| RF-LIV-005 | ❌ |  | Filtros por query params combinados com E, via `Specification` no adapter ([DEC-07](../decisoes/DEC-07-convencoes-da-api-rest.md)). |
| RF-LIV-006 | ❌ |  |  |
| RNF-LIV-001 | 🟡 | `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/persistencia/LivroJpa.java` | O código é o id do livro (`Long` por sequence); o ISBN é atributo ([DEC-14](../decisoes/DEC-14-ids-nomes-e-seed.md)). |
| RNF-LIV-002 | 🟡 | `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/infraestrutura/seed/ExecutorDeSeeds.java`, `apps/server/src/main/resources/seed/001_dominios.sql` | Autor, editora, categoria, grupo de precificação e fornecedor em `001_dominios.sql`, com IDs explícitos de 1 a 9999 ([DEC-14](../decisoes/DEC-14-ids-nomes-e-seed.md)). |
| RN-LIV-001 | 🟡 | `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/persistencia/LivroJpa.java`, `apps/server/src/main/resources/seed/002_livros.sql` | Colunas obrigatórias (`nullable = false`) no schema; livros inseridos pelo seed (CRUD fora do MVP). |
| RN-LIV-002 | 🟡 | `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/persistencia/LivroJpa.java` | `livro_categoria` muitos-para-muitos; o seed tem livros com mais de uma categoria. |
| RN-LIV-003 | ❌ |  |  |
| RN-LIV-004 | ❌ |  |  |
| RN-LIV-005 | ❌ |  |  |
| RN-LIV-006 | ❌ |  |  |
| RN-LIV-007 | ❌ |  |  |
