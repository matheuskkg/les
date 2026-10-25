# DEC-17 — Organização do código do web

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-24

## Contexto

O `server` tem arquitetura documentada (módulos hexagonais, uma regra por arquivo, ArchUnit). O `web` cresceu sem
equivalente: `src/` plano com regras, contextos e testes misturados, `paginas/` e `componentes/` genéricos, um único
`estilos.css` com ~1100 linhas, marcação repetida (botões, painéis, etiquetas de status) em vez de componentes e cada
tela reimplementando o carregamento de dados, o que deixava telas em branco enquanto a API respondia.

## Decisão

Módulos por funcionalidade, espelhando os módulos do `server`, mais uma camada de UI compartilhada:

```
apps/web/src/
├── main.tsx, app.tsx      entrada (provedores de sessão, notificações e carrinho) e rotas
├── api/                   cliente openapi-fetch e tipos gerados
├── layout/                cabeçalho e página não encontrada, usados só pela composição
├── compartilhado/         utilitários puros e hooks genéricos (formatação, carregamento de dados)
├── ui/                    componentes de apresentação sem regra de negócio (Botao, Painel, Campo, Esqueleto...)
├── estilos/               tokens, base e estilos dos componentes de ui/
└── modulos/
    ├── sessao/            login, contexto de sessão, rotas protegidas, destino após o login
    ├── catalogo/          vitrine e detalhe do livro
    ├── carrinho/
    ├── checkout/
    ├── conta/             cadastro e área do cliente
    ├── pedidos/           detalhe, trocas e linha do tempo do pedido, usados pela conta e pelo admin
    ├── notificacoes/
    └── admin/             pedidos, trocas, clientes e análise
```

Dentro de cada módulo:

- `paginas/`: componentes de rota.
- `componentes/`: componentes usados só pelo módulo.
- `regras/`: funções puras (validação, filtros, cálculos), uma por arquivo ou por assunto, com o teste ao lado
  (`*.test.ts`, runner do Node).
- `hooks/`: estado de uma tela grande, separado da marcação (o checkout tem `useEntregaDaCompra` e
  `usePagamentoDaCompra`, e cada passo é um componente que recebe esse estado).
- `api.ts` e `contexto*.tsx`: chamadas e estado compartilhado do módulo, quando há (sessão, carrinho,
  notificações).
- `index.ts`: o que o módulo expõe aos outros.
- `<modulo>.css`: estilos próprios, importado pelo `index.ts`.

Regras de dependência, verificadas por um teste (`src/arquitetura.test.ts`, papel do ArchUnit no `server`):

- `ui/` e `compartilhado/` não importam `modulos/` nem `layout/`.
- `ui/` não chama a API (`api/cliente` só como tipo): recebe dados por props.
- Um módulo só importa outro pelo `index.ts` dele ou pelas `regras/` dele, que são puras.
- Módulos não importam `layout/`, `app` nem `main`.
- `regras/` não importa Preact, `ui/`, páginas nem componentes, e só tipos de `api/cliente`.

Telas que só leem dados usam o hook `useCarregamento` de `compartilhado/`, que cancela a busca anterior e expõe
dados, problema e carregando; a tela mostra `Esqueleto` enquanto carrega. Telas com estado próprio (carrinho,
checkout) guardam o carregamento no contexto ou no hook do módulo e mostram o mesmo `Esqueleto`.

### Acréscimos da API para o novo front

- `GET /api/livros?q=`: busca livre por título, autor, editora ou ISBN (OU entre eles, E com os demais filtros).
- `Sessao.nome`: nome de exibição no cabeçalho, vindo do `clientes` pela porta `NomeDoUsuario` do `acesso`.
- `GET /api/admin/pedidos/contagem`: quantidade de pedidos e de trocas por status, para as abas do admin.

O visitante que tenta pôr um livro no carrinho tem a escolha guardada na aba (`sessionStorage`) e, depois do login,
volta ao livro com ele já no carrinho.

O preço de venda é calculado em Java a partir do estoque ([DEC-15.3](DEC-15.3-saldo-de-estoque.md)), então a
vitrine não filtra nem ordena por preço.

## Alternativas consideradas

- Manter `paginas/` e `componentes/` e só quebrar arquivos grandes: não separa regras de tela nem deixa claro onde
  algo novo deve ficar.
- CSS Modules: o Vite suporta sem dependência nova, mas muda todos os seletores usados nos estilos e nos E2E; CSS
  puro por módulo, com prefixo de classe, resolve o tamanho do arquivo único.
- Biblioteca de componentes: contraria a lista fechada de dependências ([DEC-01](DEC-01-stack-do-web.md)).

## Consequências

- O script `npm test` passa a procurar testes em qualquer profundidade de `src/`.
- Novas telas e componentes seguem a estrutura acima; `docs/arquitetura.md` continua cobrindo só o `server`.
