# Roteiro de revisão: `apps/web`

Abra esta página quando o diff toca `apps/web`. Ela organiza a leitura; a regra vale o que estiver no documento
apontado, não o resumo daqui.

## Leitura obrigatória

1. `docs/decisoes/DEC-17-organizacao-do-web.md` — estrutura de pastas, regras de dependência, carregamento de dados.
2. `docs/decisoes/DEC-01-stack-do-web.md` — lista fechada de dependências.
3. `apps/web/src/arquitetura.test.ts` — as regras de dependência executáveis.
4. `docs/decisoes/DEC-07-convencoes-da-api-rest.md` quando o diff consome ou altera a API.
5. `AGENTS.md` da raiz — convenções comuns, formatação de moeda e escopo do MVP.

## O que conferir

### Estrutura e dependências

- Módulos por funcionalidade sob `src/modulos/`; dentro de cada um, `paginas/`, `componentes/`, `regras/`, `hooks/`,
  `api.ts`, `contexto*.tsx`, `index.ts` e `<modulo>.css`.
- `ui/` e `compartilhado/` não importam `modulos/` nem `layout/`. `ui/` não chama API: recebe dados por props; o
  cliente openapi-fetch entra só como tipo.
- Um módulo importa outro apenas pelo `index.ts` ou pelas `regras/` dele. Módulos não importam `layout/`, `app` nem
  `main`.
- `regras/` são funções puras: não importam Preact, `ui/`, páginas nem componentes, só tipos de `api/cliente`. Teste
  ao lado (`*.test.ts`).
- CSS puro por módulo, com prefixo de classe. Não existe `estilos.css` único; `ui/` tem seus estilos em `estilos/`.

### Stack e UI

- Dependências apenas as de DEC-01: `preact`, `preact-iso`, `uplot`; no dev, `vite`, `@preact/preset-vite`,
  `typescript`, `@playwright/test`. Biblioteca nova só com justificativa registrada.
- Formulários nativos com validação de formato na UI. Regra de negócio continua no `server`; a UI repete só formato.
- Telas que só leem dados usam `useCarregamento` de `compartilhado/` e mostram `Esqueleto` enquanto carrega. Telas com
  estado próprio guardam carregamento no contexto ou hook do módulo e mostram o mesmo `Esqueleto`.
- Dinheiro vem da API como string decimal; a UI só formata para `R$ 1.234,56`. Nunca fazer conta com `number`.
- Tipos de API vêm de `src/api/esquema.d.ts`, gerado a partir do contrato. Não declarar DTO à mão quando ele já existe.

### Contrato e rastreabilidade

- Se o diff muda a API do lado do `web`, confira se `openapi.json` e `esquema.d.ts` foram atualizados na mesma
  mudança (`npm run gerar-api`).
- Textos da UI em português; status exibidos como no DRS (por exemplo `EM PROCESSAMENTO`), mesmo quando o enum Java
  usa `_`.

### Testes

- `npm run typecheck`, `npm test` (testes em qualquer profundidade de `src/`) e, para E2E, a skill `e2e-testes`.
- Specs são roteiros de apresentação: `apresentacao<Fluxo>.spec.ts`, apoio em `fixtures.ts`. Não avalie com critério de
  regressão.

## Comandos úteis

```bash
cd apps/web
npm run typecheck
npm test
npm run gerar-api
```

## Anti-padrões específicos do `web`

- Sugerir React Query, Zod, Tailwind ou biblioteca de componentes sem uma decisão registrada.
- Componente de `ui/` chamando a API ou importando `modulos/`.
- Regra em `regras/` importando Preact ou componente.
- Tela nova buscando dados com `useEffect` próprio em vez de `useCarregamento`, deixando a tela em branco.
- Cálculo de dinheiro, desconto ou frete feito no front.
- DTO duplicado à mão no lugar do tipo gerado.
- Revisar E2E pedindo `data-testid` ou checando botão `disabled`; carregue `e2e-testes`.
