import { defineConfig } from '@playwright/test';

// A lista fechada de dependências (DEC-01) não inclui @types/node; esta declaração
// cobre apenas o uso de process.env.CI pelo Playwright.
declare const process: { env: { CI?: string } };

const PORTA_DO_WEB = 5173;
const URL_DO_WEB = `http://localhost:${PORTA_DO_WEB}`;

// Cada ação espera meio segundo para o professor acompanhar na UI. A janela visível do Chrome fica na configuração
// do jar (`playwright.apresentacao.config.ts`); aqui o navegador roda headless por padrão (ver `use.headless`).
const ESPERA_ENTRE_ACOES_MS = 500;

// DEC-02: Chrome instalado na máquina, sem baixar navegadores.
// DEC-09: o Playwright sobe o server (h2,e2e) e o Vite; o navegador fala só com o web.
export default defineConfig({
  testDir: './e2e',
  // E2E é só roteiro de apresentação; specs de regressão não entram na suíte.
  testMatch: '**/apresentacao*.spec.ts',
  fullyParallel: false,
  workers: 1,
  forbidOnly: Boolean(process.env.CI),
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: URL_DO_WEB,
    channel: 'chrome',
    // Por padrão o teste roda headless e o acompanhamento acontece dentro da UI do Playwright (snapshots e timeline),
    // sem abrir uma janela externa do Chrome. Quem quiser o navegador visível pode rodar `npm run e2e -- --headed`;
    // a apresentação contra o jar usa `playwright.apresentacao.config.ts`, que continua headed.
    headless: true,
    // Viewport fixo mantém o layout previsível na UI e no CI.
    viewport: { width: 1280, height: 720 },
    launchOptions: {
      args: [],
      slowMo: process.env.CI ? 0 : ESPERA_ENTRE_ACOES_MS,
    },
    // No CI não há o ffmpeg que o Playwright usa para gravar vídeo (o Chrome já vem no runner, mas o ffmpeg não).
    // Trace e screenshot de falha continuam indo para o relatório enviado como artifact.
    video: process.env.CI ? 'off' : 'retain-on-failure',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chrome',
      use: { channel: 'chrome' },
    },
  ],
  webServer: [
    {
      command: 'mvn -q spring-boot:run -Dspring-boot.run.profiles=h2,e2e,dev',
      cwd: '../server',
      // Espera um livro do seed: o /v3/api-docs responde antes de o ApplicationRunner terminar de inserir os dados.
      url: 'http://localhost:8080/api/livros/1',
      timeout: 180_000,
      reuseExistingServer: !process.env.CI,
    },
    {
      command: 'npm run dev',
      cwd: '.',
      url: URL_DO_WEB,
      timeout: 120_000,
      reuseExistingServer: !process.env.CI,
    },
  ],
});
