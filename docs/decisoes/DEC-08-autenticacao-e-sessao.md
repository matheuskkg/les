# DEC-08 — Autenticação, sessão e modelo de usuário

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-21
- **Card:** #61
- **Requisitos:** RNF-CLI-003, RNF-GER-002, RF-CLI-003, RF-CLI-008

## Contexto

A API ([DEC-07](DEC-07-convencoes-da-api-rest.md)) tem rotas públicas, de cliente (`/api/me`) e de administrador
(`/api/admin`), com os papéis `CLIENTE` e `ADMIN` de `docs/arquitetura.md`. O DRS pede senha criptografada
(RNF-CLI-003), inativação de cliente (RF-CLI-003) e log com o usuário responsável por toda escrita (RNF-GER-002), mas
não define login, sessão nem quem é o administrador.

## Decisão

**Premissa:** o `web` e o `server` são servidos na mesma origem (definido no DEC-09). Por isso não há CORS, e o
cookie da sessão funciona com `SameSite=Strict`.

### Token

- JWT emitido e validado pelo `spring-boot-starter-oauth2-resource-server` (Nimbus): o `JwtEncoder` emite no login e
  o resource server valida em cada requisição.
- Algoritmo HS256. O segredo vem de `livraria.jwt.segredo` (variável `JWT_SEGREDO`), com no mínimo 32 bytes; o
  `application.yml` traz um valor fixo de desenvolvimento.
- Claims: `sub` (id do usuário), `papel` (`CLIENTE` ou `ADMIN`), `iss` (`livraria`), `iat` e `exp`.
- Validade de 8 h (`livraria.jwt.validade`), sem refresh token. Expirou, o `web` volta ao login.

### Cookie e CSRF

- O token vai num cookie `sessao`: `HttpOnly`, `SameSite=Strict`, `Path=/api`, `Max-Age` igual à validade do token.
  O atributo `Secure` é ligado por `livraria.sessao.cookie-seguro` (desligado em desenvolvimento, que usa HTTP).
- O resource server lê o token desse cookie por um `BearerTokenResolver` próprio; não há header `Authorization`.
- CSRF do Spring Security ligado, no padrão para SPA: `CookieCsrfTokenRepository` com o cookie `XSRF-TOKEN` legível
  pelo JavaScript, e o `web` o devolve no header `X-XSRF-TOKEN` em toda requisição de escrita (inclusive o login).
- O `web` chama `GET /api/sessao` ao iniciar: a resposta diz quem está logado (ou 401) e emite o `XSRF-TOKEN`.

### Rotas de sessão

| Rota | Efeito |
|---|---|
| `POST /api/sessao` | login com `{ email, senha }` (alterado pelo [DEC-10](DEC-10-cliente-enderecos-e-cartoes.md): `{ login, senha }`, por e-mail ou telefone); grava o cookie e devolve `{ usuarioId, email, papel }` |
| `GET /api/sessao` | usuário logado `{ usuarioId, email, papel }`, ou 401 |
| `DELETE /api/sessao` | logout: apaga o cookie (`Max-Age=0`) |

Login inválido responde 401 com a mensagem "E-mail ou senha inválidos." (alterada pelo [DEC-10](DEC-10-cliente-enderecos-e-cartoes.md): "Login ou senha
inválidos."), sem dizer qual dos dois errou.

### Autorização

| Rotas | Acesso |
|---|---|
| `GET /api/livros/**`, `POST /api/clientes` (autocadastro), `/api/sessao`, `GET /v3/api-docs` | público |
| `/api/me/**` | papel `CLIENTE` |
| `/api/admin/**` | papel `ADMIN` |
| demais rotas `/api/**` | autenticado |

401 e 403 respondem em `ProblemDetail`, como os demais erros do DEC-07.

### Modelo de usuário

- Módulo `acesso` (grupo `GER`), com a entidade **`Usuario`**: e-mail (único, usado no login), hash da senha,
  papel e situação (ativo/inativo). É a identidade de login. (Alterado pelo [DEC-10](DEC-10-cliente-enderecos-e-cartoes.md): o `Usuario` também tem o
  telefone, que serve de login; a unicidade vale entre as contas em uso; e há `inativadoEm`.)
- `Cliente` referencia o seu `Usuario` (1:1). O e-mail existe só no `Usuario`: o cadastro e a alteração do cliente
  (RF-CLI-001, RF-CLI-002) o gravam lá, pelos casos de uso do `acesso`.
- O administrador é um `Usuario` com papel `ADMIN`, sem `Cliente`, criado só pelo seed.
- Senha com BCrypt pela porta `CodificadorDeSenha` (RNF-CLI-003). A troca de senha (RF-CLI-008) é um caso de uso do
  `acesso` e mantém a sessão atual.

### Cliente inativo (RF-CLI-003)

- A situação fica no `Usuario`; inativar o cliente inativa o seu usuário.
- O login de usuário inativo responde 401 com "Cadastro inativo.". (Alterado pelo [DEC-10](DEC-10-cliente-enderecos-e-cartoes.md): em até 7 dias após a
  inativação, o login reativa a conta; depois disso ela fica encerrada e o login responde como identificador
  desconhecido.)
- A cada requisição autenticada, o conversor do JWT carrega o usuário pelo `sub` (consulta pela chave primária). Se
  ele não existir mais ou estiver inativo, responde 401, e o `web` volta ao login. O papel usado na autorização é o do
  banco, não o do token.

### Autor das escritas (RNF-GER-002)

A revisão do Envers guarda `usuarioId` (nulo quando não há usuário) e `usuario` (texto), obtidos da porta
`UsuarioAtual`:

| Situação | `usuarioId` | `usuario` |
|---|---|---|
| requisição autenticada | id do usuário | e-mail |
| requisição sem login (autocadastro) | nulo | `anonimo` |
| fora de requisição HTTP (jobs como `ExpirarReservas`, seed) | nulo | `sistema` |

## Alternativas consideradas

- jjwt com filtro escrito à mão: mais código nosso para o que o resource server já faz.
- RS256: par de chaves sem ganho com um único servidor.
- Token em `localStorage` ou `sessionStorage` com header `Authorization`: dispensaria CSRF e funcionaria entre
  domínios, mas um XSS leria o token.
- Só `SameSite=Strict`, sem token CSRF: depende apenas do navegador.
- Token curto com refresh token: exigiria endpoint de renovação, rotação e revogação.
- Credenciais dentro de `Cliente` e entidade `Administrador` separada: o login consultaria duas tabelas.
- Token válido até expirar mesmo após a inativação: o cliente inativado continuaria comprando por até 8 h.
- Usuário técnico "sistema" no seed: um registro artificial no banco só para a auditoria.

## Consequências

- O DEC-09 precisa manter `web` e `server` na mesma origem; hospedá-los em sites diferentes exigiria rever este
  registro.
- Toda requisição autenticada faz uma consulta pela chave primária do usuário.
- O cadastro de cliente cria o `Usuario` e o `Cliente` na mesma transação.
- Os testes de controller enviam o cookie de sessão e o token CSRF (`csrf()` do `spring-security-test`).
