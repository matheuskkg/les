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
| RF-LIV-005 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/web/LivroController.java`, `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/persistencia/CatalogoConsultasJpa.java`, `apps/web/src/modulos/catalogo/paginas/Vitrine.tsx`, `apps/web/src/modulos/catalogo/paginas/Livro.tsx`, `apps/web/src/modulos/catalogo/regras/filtrosDeLivros.ts`, `apps/web/src/compartilhado/url.ts`, `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/persistencia/EspecificacoesDeLivro.java` (busca livre `q`), `apps/web/src/layout/Cabecalho.tsx` (busca do cabeçalho) | Filtros por query params combinados com E, via `Specification` no adapter ([DEC-07](../decisoes/DEC-07-convencoes-da-api-rest.md)): código, título, autor, categoria (várias, OU), editora, edição, ISBN, código de barras, ano e páginas. O preço é calculado e não é filtro. Busca livre `q` (OU entre título, autor, editora e ISBN; E com os demais filtros, DEC-17). Sem filtro nem ordenação por preço, calculado a partir do estoque. A busca da vitrine (filtros, ordem e página) acompanha o link do livro, e o "Voltar para a vitrine" do detalhe a reconstrói. |
| RF-LIV-006 | ❌ |  |  |
| RNF-LIV-001 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/persistencia/LivroJpa.java`, `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/web/LivroController.java` | O código é o id do livro (`Long` por sequence); o ISBN é atributo ([DEC-14](../decisoes/DEC-14-ids-nomes-e-seed.md)). |
| RNF-LIV-002 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/compartilhado/infraestrutura/seed/ExecutorDeSeeds.java`, `apps/server/src/main/resources/seed/001_dominios.sql`, `apps/server/src/main/resources/seed/002_livros.sql`, `apps/server/src/main/resources/seed/003_estoque.sql`, `apps/server/src/main/resources/seed/010_dominios_ampliados.sql`, `apps/server/src/main/resources/seed/011_livros_ampliados.sql`, `apps/server/src/main/resources/seed/012_estoque_ampliado.sql`, `apps/server/src/main/resources/seed/100_usuarios.sql`, `apps/server/src/main/resources/seed/200_bandeiras.sql`, `apps/server/src/main/resources/seed/201_clientes.sql`, `apps/server/src/main/resources/seed/300_cupons.sql`, `apps/server/src/main/resources/seed/400_pedidos_historicos.sql`, `apps/server/src/main/resources/seed/500_provedores_ia.sql`, `apps/server/scripts/gerar_seed_historico.py` | Scripts executados em ordem pelo `ExecutorDeSeeds`, com IDs explícitos de 1 a 9999 ([DEC-14](../decisoes/DEC-14-ids-nomes-e-seed.md)). Domínios: 29 autores, 15 editoras, 17 categorias, 5 grupos de precificação, 10 fornecedores e 5 bandeiras. Catálogo com 56 livros e 73 entradas de estoque; 10 clientes com endereço, cartão e senha BCrypt, 3 administradores, 9 cupons promocionais e histórico de 1180 pedidos com 147 trocas em 36 meses ([DEC-21](../decisoes/DEC-21-ampliacao-do-seed-da-alt-02.md)). Os scripts `010` a `012` acrescentam 30 livros (ids 27 a 56) e os domínios novos sem alterar os dados antes existentes. |
| RN-LIV-001 | 🟡 | `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/persistencia/LivroJpa.java`, `apps/server/src/main/resources/seed/002_livros.sql`, `apps/server/src/main/resources/seed/011_livros_ampliados.sql` | Colunas obrigatórias (`nullable = false`) no schema; 56 livros inseridos pelo seed (CRUD fora do MVP): os 26 originais, os seis de `002_livros.sql` e os 30 novos (27 a 56), com ISBN e código de barras únicos. |
| RN-LIV-002 | 🟡 | `apps/server/src/main/java/br/fatec/les/livraria/catalogo/infraestrutura/persistencia/LivroJpa.java` | `livro_categoria` muitos-para-muitos; o seed tem livros com mais de uma categoria. |
| RN-LIV-003 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/catalogo/aplicacao/casodeuso/ConsultarPrecosDeVenda.java` | A margem vem do grupo de precificação do livro (`livro.grupo_precificacao_id` obrigatório). |
| RN-LIV-004 | ❌ |  |  |
| RN-LIV-005 | ❌ |  |  |
| RN-LIV-006 | ❌ |  |  |
| RN-LIV-007 | ❌ |  |  |
