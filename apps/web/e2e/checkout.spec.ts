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
    await expect(page.getByRole('heading', { name: 'Pagamento', exact: true })).toBeVisible();
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

  test('calcula o frete pelo endereço de entrega, cadastrado ou novo (RF-VEN-004, RF-VEN-005)', async ({ page }) => {
    // Dom Casmurro pesa 0,32 kg (1 bloco); o primeiro endereço da Ana tem CEP de SP (R$ 12,00)
    await expect(page.getByTestId('frete')).toHaveText('R$ 12,00');
    await expect(page.getByTestId('total')).toHaveText('R$ 59,88');

    const entrega = page.getByRole('group', { name: 'Endereço de entrega' });
    await entrega.getByLabel('Outro endereço').check();
    await expect(page.getByTestId('frete')).toHaveText('escolha o endereço de entrega');
    await page.locator('#entrega-nome').fill('Presente');
    await page.locator('#entrega-logradouro').fill('Rua Ramos Ferreira');
    await page.locator('#entrega-numero').fill('10');
    await page.locator('#entrega-bairro').fill('Centro');
    await page.locator('#entrega-cep').fill('69005040');
    await page.locator('#entrega-cidade').fill('Manaus');
    await page.locator('#entrega-uf').selectOption('AM');
    await entrega.getByRole('button', { name: 'Usar este endereço' }).click();

    await expect(page.getByTestId('frete')).toHaveText('R$ 30,00');
    await expect(page.getByTestId('total')).toHaveText('R$ 77,88');

    await page.getByLabel('Cobrança no mesmo endereço da entrega').uncheck();
    await expect(page.getByRole('group', { name: 'Endereço de cobrança' })).toBeVisible();

    await page.getByRole('button', { name: 'Voltar ao carrinho' }).click();
    await expect(page).toHaveURL(/\/carrinho$/);
  });

  test('confere cupons de troca e o cartão pelo restante (RF-VEN-006, RF-VEN-007, RN-VEN-005, RN-VEN-006)', async ({
    page,
  }) => {
    // Dom Casmurro (R$ 47,88) + frete de SP (R$ 12,00)
    await expect(page.getByTestId('restante')).toHaveText('R$ 59,88');

    const cupons = page.getByRole('group', { name: 'Cupons de troca' });
    await cupons.getByLabel(/TROCA-ANA-20/).check();
    await cupons.getByLabel(/TROCA-ANA-35/).check();
    await expect(page.getByTestId('restante')).toHaveText('R$ 4,88');

    await cupons.getByLabel(/TROCA-ANA-40/).check();
    await expect(page.getByText(/é desnecessário/).first()).toBeVisible();
    await cupons.getByLabel(/TROCA-ANA-40/).uncheck();
    await expect(page.getByTestId('restante')).toHaveText('R$ 4,88');

    await page.getByRole('group', { name: 'Cartões' }).getByLabel(/1111/).check();
    await expect(page.locator('#cartao-1-valor')).toHaveValue('4,88');
    await page.getByRole('button', { name: 'Conferir pagamento' }).click();
    await expect(page.getByText('informe o código de segurança', { exact: false })).toBeVisible();

    await page.locator('#cartao-1-cvv').fill('123');
    await page.getByRole('button', { name: 'Conferir pagamento' }).click();
    await expect(page.getByText('Pagamento conferido: Visa **** **** **** 1111: R$ 4,88')).toBeVisible();

    await page.getByRole('button', { name: 'Voltar ao carrinho' }).click();
    await expect(page).toHaveURL(/\/carrinho$/);
  });
});
