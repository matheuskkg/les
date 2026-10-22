# DEC-01 — Stack do web

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-21
- **Card:** #8

## Contexto

`apps/web` é a SPA que consome a API JWT do `server`. Precisa de rotas, formulários (cadastro, checkout) e gráficos
(`ANA`). O critério principal é ter **poucas dependências**, para que `npm install` funcione com conexão ruim.

## Decisão

**Preact + Vite + TypeScript.**

- Rotas: `preact-iso`.
- Gráficos do `ANA`: `uplot`.
- Formulários: HTML nativo com validação própria, sem biblioteca. As regras de negócio continuam no `server`; a UI só
  repete validações de formato.
- Estilos: CSS puro, sem framework de CSS.

Lista fechada de dependências:

| Tipo | Pacotes |
|---|---|
| `dependencies` | `preact`, `preact-iso`, `uplot` |
| `devDependencies` | `vite`, `@preact/preset-vite`, `typescript`, `@playwright/test` ([DEC-02](DEC-02-framework-e2e.md)) |

## Alternativas consideradas

- React + Vite com TanStack Query, React Hook Form, Zod, Recharts e Tailwind: ecossistema maior, mas muitas
  dependências.
- TypeScript puro, sem framework: download menor, porém mais código à mão em telas como carrinho e checkout.
- Sem build (ES modules com bibliotecas vendorizadas): perde TypeScript e hot reload.
- Thymeleaf + htmx no `server`: zero npm, mas elimina o `web` como app separado e troca o JWT por sessão.
- Angular, Next.js, Vue: mais pesados ou sem vantagem para este caso.

## Consequências

- A API de componentes é a do React (hooks, JSX), então exemplos de React servem com poucas adaptações.
- Nova dependência só com justificativa registrada; prefira implementar o trecho pequeno à mão.
