import { entrarComo, expect, test } from './fixtures';

const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

test.describe('Checkout', () => {
  test.beforeEach(async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/livros/1');
    await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
    await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();
    await page.goto('/carrinho');
    await page.getByRole('button', { name: 'Ir para pagamento' }).click();
    await expect(page).toHaveURL(/\/checkout$/);
  });

  test('vai para o pagamento e volta ao carrinho com os itens (RF-VEN-003, DEC-12)', async ({ page }) => {
    await expect(page.getByRole('heading', { name: 'Pagamento' })).toBeVisible();
    await expect(page.locator('.item-da-conta', { hasText: 'Dom Casmurro' })).toContainText('R$ 47,88');

    await page.getByRole('button', { name: 'Voltar ao carrinho' }).click();

    await expect(page).toHaveURL(/\/carrinho$/);
    await expect(page.locator('.item-da-conta:not(.item-removido)', { hasText: 'Dom Casmurro' })).toBeVisible();
  });

  test('checkout vencido leva os itens aos removidos do carrinho (RN-VEN-014, RNF-VEN-001)', async ({
    page,
    ajustarRelogio,
  }) => {
    await expect(page.locator('.item-da-conta', { hasText: 'Dom Casmurro' })).toBeVisible();

    await ajustarRelogio(new Date(Date.now() + 61 * 60_000).toISOString());

    await expect(page.getByText('Nenhuma compra em andamento.')).toBeVisible();
    await page.getByRole('link', { name: 'Ir para o carrinho' }).click();
    await expect(page.locator('.item-removido', { hasText: 'Dom Casmurro' })).toBeVisible();
  });
});
