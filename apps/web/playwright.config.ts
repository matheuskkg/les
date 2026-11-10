import { defineConfig } from '@playwright/test';

// A lista fechada de dependências (DEC-01) não inclui @types/node; esta declaração
// cobre apenas o uso de process.env.CI pelo Playwright.
declare const process: { env: { CI?: string } };

const PORTA_DO_WEB = 5173;
const URL_DO_WEB = `http://localhost:${PORTA_DO_WEB}`;

// Apresentação: o Chrome fica visível e cada ação espera meio segundo. No CI, roda headless e sem atraso.
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
    // Fora do CI, o professor acompanha o Chrome; no CI não há tela.
    headless: Boolean(process.env.CI),
    launchOptions: { slowMo: process.env.CI ? 0 : ESPERA_ENTRE_ACOES_MS },
    video: 'retain-on-failure',
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
      url: 'http://localhost:8080/v3/api-docs',
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
