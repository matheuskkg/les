# DEC-07 — Convenções da API REST

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-21
- **Card:** #60
- **Requisitos:** RF-CLI-004, RF-LIV-005, RNF-GER-001

## Contexto

O `web` ([DEC-01](DEC-01-stack-do-web.md)) consome a API JSON do `server`. Faltava padronizar entre os controllers:
rotas, formatos, paginação, filtros, erros e contrato. RF-CLI-004 e RF-LIV-005 pedem consultas por qualquer
combinação dos campos de identificação, e RNF-GER-001 limita a resposta das consultas a 1 segundo.

## Decisão

### Rotas

Todas sob `/api`, com recursos no plural, em português e em kebab-case.

| Prefixo | Quem acessa | Exemplos |
|---|---|---|
| `/api/...` | público | `/api/livros`, `/api/livros/{id}` |
| `/api/me/...` | cliente autenticado (papel `CLIENTE`), sempre sobre os próprios dados | `/api/me`, `/api/me/enderecos`, `/api/me/cartoes`, `/api/me/carrinho`, `/api/me/pedidos` |
| `/api/admin/...` | administrador (papel `ADMIN`) | `/api/admin/clientes`, `/api/admin/pedidos`, `/api/admin/analise` |

O cliente nunca informa o próprio ID na URL: ele vem do usuário autenticado. Uma transição de status é uma
sub-rota com `POST`, nomeada pela ação (`POST /api/admin/pedidos/{id}/despachar`).

### Formatos no JSON

- Campos em camelCase e em português (`precoVenda`, `dataNascimento`).
- **Dinheiro:** string decimal com ponto e 2 casas (`"1234.56"`), na entrada e na saída. Os DTOs usam `Dinheiro`, e
  um (de)serializador Jackson em `compartilhado/infraestrutura/web` faz a conversão. O `web` não faz conta de
  dinheiro com `number`, só formata para `R$ 1.234,56`.
- **Datas** em ISO-8601:
  - data: `"2026-10-21"` (`LocalDate`);
  - data e hora: `"2026-10-21T14:30:00Z"` (`Instant`, em UTC; o `web` exibe no fuso local);
  - mês e ano: `"2027-08"` (`YearMonth`, por exemplo a validade do cartão).
- **Nulos:** campo nulo é omitido do JSON (`default-property-inclusion: non_null`). No TypeScript gerado, um campo
  `@Nullable` é opcional.
- **Enums:** pelo nome da constante Java (`EM_PROCESSAMENTO`). O texto exibido na UI é o do DRS
  (`EM PROCESSAMENTO`).

### Paginação e ordenação

- Parâmetros: `pagina` (a partir de 0, padrão 0), `tamanho` (padrão 20, máximo 100) e `ordem=campo,asc|desc`
  (repetível). Cada listagem define os campos ordenáveis e a ordem padrão; campo fora da lista dá 400.
- O `id` entra sempre como último critério de desempate, para que nenhum item se repita ou suma entre páginas.
- Resposta em envelope próprio:

  ```json
  { "itens": [], "pagina": 0, "tamanho": 20, "totalItens": 57, "totalPaginas": 3 }
  ```

- Os records `Paginacao` (entrada) e `Pagina<T>` (saída) ficam em `compartilhado/aplicacao`. As portas os usam, e o
  adapter JPA converte de/para `Pageable` e `Page`. Os tipos do Spring Data não saem de `infraestrutura`.

### Filtros combinados (RF-CLI-004, RF-LIV-005)

- Cada campo de identificação é um query param opcional, e os informados se combinam com E
  (`/api/admin/clientes?nome=ana&genero=FEMININO`).
- Um campo pode ser repetido para aceitar vários valores, combinados com OU dentro do campo
  (`/api/admin/pedidos?status=APROVADA&status=EM_TRANSPORTE`).
- Normalização da entrada: todo valor passa por `trim`, e parâmetro vazio é ignorado. CPF, CEP e telefone são
  comparados só pelos dígitos, com ou sem máscara.
- Valor em formato inválido ou intervalo com `De` maior que `Ate` dá 400.
- Correspondência:
  - textos livres (nome, título, autor...): "contém", sem diferenciar maiúsculas nem acentos. A coluna e o termo
    passam por `lower` e `translate` (troca das letras acentuadas pelas sem acento), funções que existem no
    PostgreSQL e no H2. O `unaccent` do PostgreSQL fica de fora porque não existe no H2;
  - identificadores e enums (CPF, ISBN, código, status): igualdade;
  - datas e valores: intervalo com sufixos `De` e `Ate` (`dataNascimentoDe`, `precoAte`).
- No "contém", os caracteres `%`, `_` e o de escape vindos do usuário são escapados, para não virarem curinga do
  `LIKE`.
- Filtros por relação muitos-para-muitos (autor, categoria) usam subconsulta com `exists`, não join: o join
  duplica linhas e quebra a contagem da paginação.
- Situação: a vitrine pública (`/api/livros`) só lista livros ativos. Nas listagens do admin, a situação
  (ativo/inativo) é um filtro como os outros.
- A porta de consulta recebe um record de filtro simples, por exemplo `FiltroCliente`. O adapter em
  `infraestrutura/persistencia` traduz esse record para uma JPA `Specification`, uma por campo, combinadas.
  `aplicacao` não conhece `Specification`.
- Toda consulta é paginada, para atender RNF-GER-001.

### Erros

Todo erro responde com `ProblemDetail` (RFC 9457, `Content-Type: application/problem+json`):

```json
{
  "type": "about:blank",
  "title": "Unprocessable Content",
  "status": 422,
  "detail": "A operação viola regras de negócio.",
  "instance": "/api/me/pedidos",
  "violacoes": [{ "codigo": "RN-VEN-004", "mensagem": "Valor mínimo de R$ 10,00 por cartão." }]
}
```

- O `TratadorDeErros` estende `ResponseEntityExceptionHandler`, que já converte as exceções do Spring MVC (JSON
  malformado, tipo inválido, rota inexistente, método ou mídia não suportados) em `ProblemDetail` com o status
  correto.
- A extensão `violacoes` só aparece no 422.
- `detail` vem em português nos erros da aplicação. Nos erros do framework, o `web` exibe a própria mensagem
  conforme o `status`.

| Status | Quando |
|---|---|
| 400 | JSON malformado, tipo ou formato inválido, parâmetro de paginação, ordenação ou filtro inválido |
| 401 | requisição sem autenticação válida |
| 403 | autenticado, mas sem o papel exigido |
| 404 | recurso inexistente, ou de outro cliente nas rotas `/api/me` |
| 409 | conflito com o estado atual: transição de status inválida ou alteração concorrente |
| 422 | violação de regra de negócio (`ExcecaoDeNegocio`), com todas as violações e o código do requisito |
| 500 | erro inesperado, com mensagem genérica e sem detalhes internos |

### Contrato

O `server` é a única fonte do contrato. O `web` gera os tipos a partir dele.

- **Server:** `springdoc-openapi-starter-webmvc-api` (linha 3.x, a compatível com o Spring Boot 4), sem Swagger UI.
  A configuração fica em `compartilhado/infraestrutura/web`; o domínio não recebe anotação de springdoc nem de
  Jackson.
  - O `ConversorDeEsquemas` (um `ModelConverter` do swagger) declara `Dinheiro` e `YearMonth` como string e marca
    como obrigatórios os componentes dos records, exceto os anotados com `@Nullable` (JSpecify), para que os tipos
    gerados não fiquem com todo campo opcional.
  - A especificação sai em OpenAPI 3.1, com chaves ordenadas e indentada, para que o diff do arquivo versionado seja
    legível.
- **Especificação versionada:** um teste de integração gera a especificação e compara com
  `apps/web/openapi.json`. Se estiver desatualizada, o teste falha; rodá-lo com `-Dlivraria.openapi.gravar=true`
  regrava o arquivo. Quem altera um DTO regenera o arquivo e os tipos no mesmo commit, e o `web` não precisa do
  `server` rodando para compilar.
- **Web:**
  - `openapi-typescript` (devDependency) gera os tipos de `openapi.json` em `apps/web/src/api/`
    (`npm run gerar-api`);
  - `openapi-fetch` (dependency) é o cliente HTTP tipado por esses tipos.
- **Exceção ao [DEC-01](DEC-01-stack-do-web.md):** esses dois pacotes entram na lista fechada. O `openapi-typescript`
  traz cerca de 30 pacotes transitivos (≈ 20 MB, quase tudo `@redocly/openapi-core`), só em tempo de
  desenvolvimento. O `openapi-fetch` tem 1 dependência e cerca de 6 KB no bundle. Em troca, o `tsc` acusa no `web`
  toda incompatibilidade com o contrato.
- O ArchUnit impede que `dominio` importe Jackson (`com.fasterxml.jackson..`, `tools.jackson..`) ou springdoc
  (`io.swagger..`, `org.springdoc..`).

## Alternativas consideradas

- Rotas por ID (`/api/clientes/{id}/...`) para cliente e admin: obrigaria toda rota a conferir se o ID é o do
  usuário logado.
- Dinheiro como número JSON: vira `float` no JavaScript. Centavos inteiros: exigiria converter nos dois lados.
- Serializar o `Page` do Spring Data: o formato não é estável, e o `Pageable` vazaria para as portas.
  Sem paginação: não garante RNF-GER-001.
- Criteria API à mão ou SQL nativo dinâmico nos filtros: mais código para o mesmo resultado.
- Ignorar acentos com `unaccent`: só existe no PostgreSQL. Com coluna normalizada gravada pelo Java: o seed em SQL
  teria de preencher essa coluna à mão.
- Corpo próprio (`{ mensagem, violacoes }`): exigiria tratar à mão cada exceção do Spring MVC que o
  `ResponseEntityExceptionHandler` já trata.
- Tipos TypeScript escritos à mão: sem dependência nova, mas o contrato dependeria de disciplina, e uma divergência
  só apareceria em execução.
- `@Schema` em `Dinheiro`: levaria o springdoc para o domínio.

## Consequências

- O `TratadorDeErros` responde `ProblemDetail`: 422 com `violacoes`, 500 genérico e os erros do Spring MVC pela
  classe-mãe. O 409 vem de uma exceção de conflito do domínio, também tratada nele. Os 401 e 403 vêm do Spring Security, que
  também deve responder `ProblemDetail`.
- Cada listagem declara os campos filtráveis e ordenáveis, e os testes cobrem a combinação de filtros.
- Mudanças em DTOs exigem regenerar `apps/web/openapi.json` e os tipos no mesmo commit.
- A lista fechada de dependências do `web` passa a ser a do [DEC-01](DEC-01-stack-do-web.md) mais `openapi-fetch` e
  `openapi-typescript`.
