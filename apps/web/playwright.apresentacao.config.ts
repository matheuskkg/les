import { defineConfig } from '@playwright/test';

// Meio segundo entre as ações; aumente se a apresentação precisar de mais tempo.
const ESPERA_ENTRE_ACOES_MS = 500;

/**
 * Configuração dos testes de apresentação contra o pacote já montado (jar servindo o web na porta 8080).
 *
 * Diferente do `playwright.config.ts` de desenvolvimento, aqui não há `webServer`: o `iniciar-app.bat` (ou o comando
 * `java -jar` do guia) sobe a aplicação antes, e o teste usa o Chrome instalado na máquina. O servidor precisa estar
 * com o profile `e2e` para expor as rotas de reset e de relógio usadas pelo `fixtures.ts`.
 *
 * Qualquer fluxo pode ter um roteiro de apresentação: basta criar `e2e/apresentacao<Fluxo>.spec.ts`. O `testMatch`
 * pega todos os arquivos desse padrão, então o pacote portátil não precisa ser alterado a cada módulo novo.
 */
export default defineConfig({
  testDir: './e2e',
  testMatch: '**/apresentacao*.spec.ts',
  fullyParallel: false,
  workers: 1,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:8080',
    channel: 'chrome',
    // Apresentação contra o jar: Chrome visível e maximizado, com meio segundo entre as ações para o professor
    // acompanhar o alvo de cada validação.
    headless: false,
    viewport: null,
    launchOptions: { args: ['--start-maximized'], slowMo: ESPERA_ENTRE_ACOES_MS },
    // Sem vídeo: a gravação exige o ffmpeg que o Playwright baixa junto dos navegadores, e o pacote portátil
    // depende só do Chrome instalado na máquina.
    video: 'off',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
});
