import { expect, test } from './fixtures';

test.describe('Vitrine', () => {
  test('lista os livros do seed', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByRole('heading', { name: 'Dom Casmurro' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Código Limpo' })).toBeVisible();
    await expect(page.getByText(/\d+ livros encontrados/)).toBeVisible();
  });

  test('filtra por título e reduz a grade', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Código Limpo' })).toBeVisible();

    await page.getByLabel('Título', { exact: true }).fill('Dom Casmurro');
    await page.getByRole('button', { name: 'Filtrar' }).click();

    await expect(page).toHaveURL(/[?&]titulo=Dom/);
    await expect(page.getByText('1 livro encontrado')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Dom Casmurro' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Código Limpo' })).toHaveCount(0);
  });

  test('abre o detalhe do livro com ficha e preço', async ({ page }) => {
    await page.goto('/');

    await page.getByRole('link', { name: 'Dom Casmurro' }).click();

    await expect(page).toHaveURL(/\/livros\/1$/);
    await expect(page.getByRole('heading', { level: 1, name: 'Dom Casmurro' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Ficha do livro' })).toBeVisible();
    await expect(page.getByText(/^R\$\s*\d/)).toBeVisible();
  });
});
