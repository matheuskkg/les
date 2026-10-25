import { entrarComo, expect, test } from './fixtures';

const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

test('a primeira carga de uma página sob demanda mostra o aviso acessível (DEC-17)', async ({ page }) => {
  await page.route('**/modulos/catalogo/paginas/Livro.tsx*', async (rota) => {
    await new Promise((resolve) => setTimeout(resolve, 800));
    await rota.continue();
  });

  await page.goto('/livros/1');

  await expect(page.getByText('Carregando a página…')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Dom Casmurro' })).toBeVisible();
});

test('navegar entre páginas sob demanda mantém a tela anterior enquanto carrega (DEC-17)', async ({
  page,
}) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Vitrine' })).toBeVisible();

  await page.route('**/modulos/catalogo/paginas/Livro.tsx*', async (rota) => {
    await new Promise((resolve) => setTimeout(resolve, 800));
    await rota.continue();
  });
  await page.getByRole('link', { name: /Dom Casmurro/ }).first().click();

  await expect(page.getByRole('heading', { name: 'Vitrine' })).toBeVisible();
  await expect(page.getByText('Carregando a página…')).toHaveCount(0);
  await expect(page.getByRole('heading', { name: 'Dom Casmurro' })).toBeVisible();
});

test('o cabeçalho anônimo em 360px mantém a vitrine e não deixa a navegação vazia', async ({ page }) => {
  await page.setViewportSize({ width: 360, height: 800 });
  await page.goto('/');

  const navegacao = page.getByRole('banner').getByRole('navigation', { name: 'Principal' });
  await expect(navegacao.getByRole('link', { name: 'Vitrine' })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(360);
});

test('o cabeçalho mantém a navegação em uma linha e sem estouro em 360px', async ({ page }) => {
  await page.setViewportSize({ width: 360, height: 800 });
  await entrarComo(page, ANA.login, ANA.senha);

  const navegacao = page.getByRole('banner').getByRole('navigation', { name: 'Principal' });
  await expect(navegacao.locator('.navegacao__link--inicio')).toBeHidden();

  const itens = navegacao.locator('.navegacao__link:visible');
  await expect(itens).toHaveCount(3);

  const caixas = await itens.evaluateAll((elementos) =>
    elementos.map((elemento) => {
      const caixa = elemento.getBoundingClientRect();
      return { centro: caixa.top + caixa.height / 2, direita: Math.round(caixa.right) };
    }),
  );
  const centros = caixas.map((caixa) => caixa.centro);
  expect(Math.max(...centros) - Math.min(...centros)).toBeLessThanOrEqual(2);
  expect(Math.max(...caixas.map((caixa) => caixa.direita))).toBeLessThanOrEqual(360);
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(360);
});
