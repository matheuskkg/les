import { entrarComo, expect, test } from './fixtures';

test.describe('Consulta de clientes do administrador', () => {
  test('filtra por nome e abre o detalhe do cliente', async ({ page }) => {
    await entrarComo(page, 'admin@livraria.test', 'Admin@123');
    await page.goto('/admin/clientes');

    await expect(page.getByRole('heading', { name: 'Clientes' })).toBeVisible();
    await expect(page.getByText('5 clientes encontrados')).toBeVisible();

    await page.getByLabel('Nome', { exact: true }).fill('souza');
    await page.getByRole('button', { name: 'Filtrar' }).click();

    await expect(page).toHaveURL(/nome=souza/);
    await expect(page.getByText('1 cliente encontrado')).toBeVisible();

    await page.getByRole('link', { name: 'Ana Souza' }).click();
    await expect(page.getByRole('heading', { name: 'Ana Souza' })).toBeVisible();
    await expect(page.getByText('529.982.247-25')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Endereços' })).toBeVisible();
  });
});
