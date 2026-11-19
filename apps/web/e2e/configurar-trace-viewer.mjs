/**
 * Habilita "Display canvas content" na UI do Playwright antes de abrir os testes com `npm run e2e`.
 *
 * A preferência não tem opção em `playwright.config.ts`: a UI Mode e o Trace Viewer guardam as configurações da
 * própria interface em `.settings/traceviewer.json`, dentro do diretório de browsers do Playwright, e leem esse
 * arquivo ao abrir. Desligada, a UI desenha um xadrez no lugar do canvas do uPlot (issue #23964 do Playwright) e o
 * gráfico de análise aparece como placeholder, embora no Chrome e nas capturas de tela ele esteja normal.
 *
 * O valor é a string "true" porque o Playwright serializa o localStorage como texto e a UI interpreta com
 * JSON.parse. As preferências já salvas (largura da sidebar, watch etc.) são preservadas.
 */
import coreBundle from 'playwright-core/lib/coreBundle';
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';

const arquivo = join(coreBundle.registry.registryDirectory, '.settings', 'traceviewer.json');

let preferencias = {};
try {
  const lidas = JSON.parse(readFileSync(arquivo, 'utf8'));
  if (lidas && typeof lidas === 'object' && !Array.isArray(lidas)) {
    preferencias = lidas;
  }
} catch {
  // Primeira execução: ainda não existe arquivo de preferências.
}

mkdirSync(dirname(arquivo), { recursive: true });
writeFileSync(arquivo, JSON.stringify({ ...preferencias, shouldPopulateCanvasFromScreenshot: 'true' }));
