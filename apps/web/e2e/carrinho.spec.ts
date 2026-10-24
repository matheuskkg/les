import { entrarComo, expect, test } from './fixtures';

const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

test.describe('Carrinho', () => {
  test('visitante que adiciona um livro vai ao login', async ({ page }) => {
    await page.goto('/livros/1');
    await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
    await expect(page).toHaveURL(/\/login\?destino=%2Flivros%2F1/);
  });

  test('cliente adiciona, altera a quantidade e remove um livro', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/livros/1');
    await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
    await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();

    await page.getByRole('banner').getByRole('link', { name: 'Carrinho' }).click();
    const linha = page.locator('.item-da-conta', { hasText: 'Dom Casmurro' });
    await expect(linha).toContainText('R$ 47,88');

    const quantidade = linha.getByLabel('Quantidade');
    await quantidade.fill('3');
    await quantidade.press('Enter');
    await expect(page.getByText('R$ 143,64').first()).toBeVisible();

    await linha.getByRole('button', { name: 'Remover' }).click();
    await expect(page.getByText('Seu carrinho está vazio.')).toBeVisible();
  });

  test('quantidade acima do estoque mostra a violação RN-VEN-001', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/livros/7');
    await page.getByLabel('Quantidade').fill('6');
    await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
    await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();

    await page.getByLabel('Quantidade').fill('1');
    await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
    await expect(page.getByText(/Estoque insuficiente/)).toBeVisible();
  });

  test('itens vencidos saem do carrinho e podem ser adicionados de novo (RN-VEN-014, RNF-VEN-001)', async ({
    page,
    ajustarRelogio,
  }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/livros/1');
    await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
    await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();
    await page.goto('/carrinho');
    await expect(page.locator('.item-da-conta', { hasText: 'Dom Casmurro' })).toBeVisible();

    // Prazo de 30 min: a tarefa de expiração roda ao ajustar o relógio, e a notificação recarrega a tela.
    await ajustarRelogio(new Date(Date.now() + 31 * 60_000).toISOString());

    const removido = page.locator('.item-removido', { hasText: 'Dom Casmurro' });
    await expect(removido).toBeVisible();
    await expect(page.getByText('Seu carrinho está vazio.')).toBeVisible();
    await expect(removido.getByRole('button', { name: 'Comprar' })).toBeDisabled();

    await removido.getByRole('button', { name: 'Adicionar de novo' }).click();
    await expect(page.locator('.item-removido')).toHaveCount(0);
    await expect(page.locator('.item-da-conta', { hasText: 'Dom Casmurro' })).toContainText('R$ 47,88');
  });
});
