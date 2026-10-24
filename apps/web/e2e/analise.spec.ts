import { entrarComo, expect, test } from './fixtures';

// Março de 2025 do seed histórico (scripts/gerar_seed_historico.py): Romance e História R$ 95,76 (dois Dom
// Casmurro), Ficção Científica R$ 96,00 (uma unidade TROCADO descontada), Fantasia R$ 80,00 (troca ainda autorizada)
// e Tecnologia R$ 0,00 (o único pedido foi REPROVADA).
const MARCO = '/admin/analise?inicio=2025-03&fim=2025-04';

test.beforeEach(async ({ page }) => {
  await entrarComo(page, 'admin@livraria.test', 'Admin@123');
});

test('admin vê o gráfico e a tabela de vendas por categoria e mês (RF-ANA-001, RN-ANA-001, RN-ANA-003, RN-ANA-004)', async ({
  page,
}) => {
  await page.goto(MARCO);
  await expect(page.getByRole('heading', { name: 'Evolução de 03/2025 a 04/2025' })).toBeVisible();
  await expect(page.getByRole('img', { name: /Gráfico de linhas/ })).toBeVisible();
  await expect(page.getByRole('list', { name: 'Legenda' }).getByRole('listitem')).toHaveText([
    'Fantasia',
    'Ficção Científica',
    'História',
    'Romance',
    'Tecnologia',
  ]);

  const marco = page.getByRole('row', { name: /^03\/2025/ });
  await expect(marco.getByRole('cell')).toHaveText(['R$ 80,00', 'R$ 96,00', 'R$ 95,76', 'R$ 95,76', 'R$ 0,00']);
  await expect(page.getByRole('row', { name: /^04\/2025/ })).toBeVisible();

  // RNF-ANA-004: o tooltip mostra o valor de cada categoria no mês sob o cursor.
  await page.locator('.grafico__area .u-over').hover({ position: { x: 4, y: 40 } });
  await expect(page.locator('.grafico__tooltip')).toContainText('03/2025');
  await expect(page.locator('.grafico__tooltip')).toContainText('R$ 96,00');
});

test('admin compara só as categorias escolhidas e exporta o CSV com os mesmos filtros (RF-ANA-003, RF-ANA-004)', async ({
  page,
}) => {
  await page.goto(MARCO);
  const categorias = page.getByRole('group', { name: 'Categorias' });
  await categorias.getByLabel('Romance').uncheck();
  await categorias.getByLabel('Fantasia').uncheck();
  await categorias.getByLabel('Tecnologia').uncheck();
  await categorias.getByLabel('História').uncheck();
  await page.getByRole('button', { name: 'Atualizar' }).click();
  await expect(page).toHaveURL(/categoria=2$/);
  await expect(page.getByRole('list', { name: 'Legenda' }).getByRole('listitem')).toHaveText(['Ficção Científica']);

  const [download] = await Promise.all([
    page.waitForEvent('download'),
    page.getByRole('button', { name: 'Exportar CSV' }).click(),
  ]);
  expect(download.suggestedFilename()).toBe('analise-de-vendas-2025-03-a-2025-04.csv');
  const caminho = await download.path();
  const csv = (await import('node:fs')).readFileSync(caminho, 'utf-8');
  expect(csv.startsWith('\uFEFFmes;categoria;valor\r\n2025-03;Ficção Científica;96,00\r\n')).toBe(true);

  await categorias.getByLabel('Ficção Científica').uncheck();
  await page.getByRole('button', { name: 'Atualizar' }).click();
  await expect(page.getByRole('alert')).toHaveText('Selecione ao menos uma categoria.');
});

test('período invertido ou maior que 24 meses mostra a regra violada (RF-ANA-002, RN-ANA-002)', async ({ page }) => {
  await page.goto('/admin/analise?inicio=2025-06&fim=2025-03');
  await expect(page.getByRole('alert')).toContainText('RF-ANA-002');

  await page.getByLabel('Mês inicial').fill('2024-10');
  await page.getByLabel('Mês final').fill('2026-10');
  await page.getByRole('button', { name: 'Atualizar' }).click();
  await expect(page.getByRole('alert')).toContainText('RN-ANA-002');
});
