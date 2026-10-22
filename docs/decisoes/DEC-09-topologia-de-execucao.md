# DEC-09 — Topologia de execução de web e server

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-21
- **Card:** #62

## Contexto

O `web` ([DEC-01](DEC-01-stack-do-web.md)) e o `server` são aplicações separadas, mas a sessão do
[DEC-08](DEC-08-autenticacao-e-sessao.md) usa cookie `SameSite=Strict` e token CSRF lido pelo JavaScript, o que exige
que os dois sejam servidos na mesma origem. Os E2E ([DEC-02](DEC-02-framework-e2e.md)) precisam subir os dois com
dados previsíveis.

## Decisão

Em todos os cenários, o navegador vê uma única origem, e o `web` chama a API por URLs relativas (`/api/...`). Não há
variável com a URL da API nem CORS.

### Desenvolvimento

| Processo | Porta | Comando |
|---|---|---|
| `server` | 8080 | `mvn spring-boot:run` em `apps/server` |
| `web` (Vite) | 5173 | `npm run dev` em `apps/web` |

O `vite.config.ts` encaminha `/api` e `/v3` para `http://localhost:8080` (`server.proxy`). O navegador acessa
`http://localhost:5173`.

### Demonstração e entrega

Um único processo: o Spring serve o build do `web`.

1. `npm run build` em `apps/web` gera `apps/web/dist`.
2. O `mvn package` copia `apps/web/dist` para `static/` dentro do jar, se o diretório existir
   (`maven-resources-plugin`, execução `copiar-build-do-web`). O Maven não baixa Node: sem o build do `web`, o jar
   sai só com a API.
3. `java -jar` sobe tudo em `http://localhost:8080`.

As rotas do `web` (`preact-iso`) são do lado do cliente. Por isso, um `GET` fora de `/api` e `/v3` cujo último
segmento não tenha extensão devolve o `index.html` (`ConfiguracaoDoWeb`); arquivo inexistente e rota inexistente da
API dão 404. Essas rotas e os arquivos estáticos são públicos.

### Testes E2E

- O `playwright.config.ts` sobe os dois processos pelo `webServer`:
  - `server` com os profiles `h2,e2e`, aguardando `http://localhost:8080/v3/api-docs`;
  - `web` com `npm run dev`, aguardando `http://localhost:5173`, que é a `baseURL` dos testes.
  - Localmente reaproveita processos já no ar (`reuseExistingServer`); no CI sobe sempre.
- Um worker só (`workers: 1`), porque os testes compartilham o banco.
- O profile `e2e` expõe rotas de apoio, que não existem fora dele:

  | Rota | Efeito |
  |---|---|
  | `POST /api/teste/reset` | apaga os dados de todas as tabelas (inclusive as `_aud` do Envers e `seed_execucao`), reexecuta o seed e volta o relógio ao do sistema |
  | `POST /api/teste/relogio` | fixa a data e hora da porta `Relogio` (`{ "agora": "2026-10-21T14:30:00Z" }`, convertido para o fuso `America/Sao_Paulo`) e executa em seguida as tarefas que dependem do tempo |

- Tarefas periódicas que dependem do relógio, como `ExpirarReservas`, implementam `TarefaPorTempo`
  (`compartilhado/aplicacao`), e é assim que o `POST /api/teste/relogio` as encontra.
- Cada teste chama `POST /api/teste/reset` no `beforeEach` e começa do seed.
- O profile `e2e` só sobe junto do `h2`; combinado com `postgres`, a aplicação não inicia.

## Alternativas consideradas

- CORS com o `web` chamando `http://localhost:8080`: o cookie `SameSite=Strict` e o `XSRF-TOKEN` do DEC-08 não
  funcionariam.
- Dois processos na demonstração (`vite preview` com proxy): funciona, mas são dois processos para subir.
- `frontend-maven-plugin`: o Maven baixaria Node (≈ 50 MB), contra a restrição de conexão do DEC-01.
- Banco novo por execução, sem reset: os testes interfeririam entre si (estoque, cupons) e não haveria como
  controlar o relógio para a expiração do carrinho.

## Consequências

- O cookie de sessão (`Path=/api`) funciona igual nos três cenários, pois o caminho chega intacto pelo proxy.
- O apoio aos E2E fica em `compartilhado/infraestrutura/e2e`, com beans anotados com `@Profile("e2e")`.
- Testes E2E não rodam em paralelo.
