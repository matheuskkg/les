import { entrarComo, expect, test } from './fixtures';

test.describe('Notificações', () => {
  test('administrador abre a página pelo cabeçalho e vê o histórico vazio', async ({ page }) => {
    await entrarComo(page, 'admin@livraria.test', 'Admin@123');

    await page.getByRole('banner').getByRole('link', { name: 'Notificações' }).click();

    await expect(page).toHaveURL(/\/admin\/notificacoes$/);
    await expect(page.getByRole('heading', { name: 'Notificações' })).toBeVisible();
    await expect(page.getByText('Nenhuma notificação por enquanto.')).toBeVisible();
  });
});
