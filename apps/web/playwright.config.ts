import { defineConfig } from '@playwright/test';

// A lista fechada de dependências (DEC-01) não inclui @types/node; esta declaração
// cobre apenas o uso de process.env.CI pelo Playwright.
declare const process: { env: { CI?: string } };

const PORTA_DO_WEB = 5173;
const URL_DO_WEB = `http://localhost:${PORTA_DO_WEB}`;

// DEC-02: Chrome instalado na máquina, sem baixar navegadores.
// DEC-09: o Playwright sobe o server (h2,e2e) e o Vite; o navegador fala só com o web.
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  forbidOnly: Boolean(process.env.CI),
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: URL_DO_WEB,
    channel: 'chrome',
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
      command: 'mvn -q spring-boot:run -Dspring-boot.run.profiles=h2,e2e',
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
