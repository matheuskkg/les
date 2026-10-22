# DEC-02 — Framework de testes E2E

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-21
- **Card:** #9

## Contexto

Os fluxos principais (cadastro, compra, troca) precisam de testes E2E, e a gravação deles ajuda na apresentação. A
conexão ruim também limita o download de navegadores.

## Decisão

**Playwright** (`@playwright/test`), com vídeo e trace dos fluxos.

Os testes usam o Google Chrome instalado na máquina (`use: { channel: 'chrome' }` no `playwright.config.ts`); não se
roda `npx playwright install`. O runner do GitHub Actions já traz o Chrome.

## Alternativas consideradas

- Cypress: gravação de vídeo e paralelismo mais limitados na versão gratuita.
- Selenium: poderia ser em Java junto do `server`, mas é mais verboso e frágil.

## Consequências

- Quem roda os E2E precisa do Google Chrome instalado.
- Os testes ficam no `web`, que precisa estar rodando contra o `server` com seed.
