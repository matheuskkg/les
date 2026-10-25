import { entrarComo, expect, test } from './fixtures';

const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

test.describe('Endereços e cartões da conta', () => {
  test('cliente adiciona e exclui um endereço', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/minha-conta/enderecos');

    await page.getByRole('button', { name: 'Adicionar endereço' }).click();
    await page.locator('#endereco-nome').fill('Casa da praia');
    await page.locator('#endereco-tipo').selectOption('CASA');
    await page.locator('#endereco-logradouro-tipo').selectOption('AVENIDA');
    await page.locator('#endereco-logradouro').fill('Avenida Beira Mar');
    await page.locator('#endereco-numero').fill('500');
    await page.locator('#endereco-bairro').fill('Centro');
    await page.locator('#endereco-cep').fill('11010000');
    await page.locator('#endereco-cidade').fill('Santos');
    await page.locator('#endereco-uf').selectOption('SP');
    await page.getByRole('button', { name: 'Salvar endereço' }).click();

    const item = page.locator('.item-da-conta', { hasText: 'Casa da praia' });
    await expect(item).toBeVisible();
    await item.getByRole('button', { name: 'Excluir' }).click();
    await expect(item).toHaveCount(0);
  });

  test('cliente adiciona um cartão e o torna preferencial', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/minha-conta/cartoes');

    await page.getByRole('button', { name: 'Adicionar cartão' }).click();
    await page.locator('#cartao-numero').fill('5555 5555 5555 4444');
    await page.locator('#cartao-nome').fill('ANA SOUZA');
    await page.locator('#cartao-validade').fill('2030-01');
    await page.getByRole('button', { name: 'Salvar cartão' }).click();

    const novo = page.locator('.item-da-conta', { hasText: 'Mastercard' });
    await expect(novo).toContainText('4444');
    await expect(novo).not.toContainText('5555555555554444');
    await novo.getByRole('button', { name: 'Tornar preferencial' }).click();
    await expect(novo).toContainText('Preferencial');
  });
});
