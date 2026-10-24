import { entrarComo, expect, test } from './fixtures';

// Elisa tem um cartão vencido (final 0005) e um terminado em 0000 (DEC-05.2); o primeiro endereço dela é em Porto
// Alegre (frete R$ 18,00).
const ELISA = { login: 'elisa.prado@livraria.test', senha: 'Senha@123' };

test('reprova pela operadora, mantém a compra e aprova com outro cartão (RF-VEN-008, RN-VEN-007, RN-VEN-008)', async ({
  page,
}) => {
  await entrarComo(page, ELISA.login, ELISA.senha);
  await page.goto('/livros/1');
  await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
  await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();
  await page.goto('/carrinho');
  await page.getByRole('button', { name: 'Ir para pagamento' }).click();
  await expect(page.getByTestId('restante')).toHaveText('R$ 65,88');

  const cartoes = page.getByRole('group', { name: 'Cartões' });
  await cartoes.getByLabel(/0005/).check();
  await page.locator('#cartao-4-cvv').fill('1234');
  await page.getByRole('button', { name: 'Finalizar compra' }).click();
  await expect(page.getByTestId('reprovacao')).toContainText('Cartão final 0005: Cartão vencido');

  await cartoes.getByLabel(/0005/).uncheck();
  await cartoes.getByLabel(/0000/).check();
  await page.locator('#cartao-5-cvv').fill('123');
  await page.getByRole('button', { name: 'Finalizar compra' }).click();
  await expect(page.getByTestId('reprovacao')).toContainText(
    'Cartão final 0000: Transação recusada pela operadora',
  );

  await cartoes.getByLabel(/0000/).uncheck();
  await cartoes.getByLabel('Outro cartão').check();
  await page.locator('#cartao-novo-numero').fill('4012888888881881');
  await page.locator('#cartao-novo-nome').fill('ELISA PRADO');
  await page.locator('#cartao-novo-bandeira').selectOption({ label: 'Visa' });
  await page.locator('#cartao-novo-validade').fill('2031-06');
  await expect(page.locator('#cartao-novo-valor')).toHaveValue('65,88');
  await page.locator('#cartao-novo-cvv').fill('321');
  await page.getByRole('button', { name: 'Finalizar compra' }).click();

  await expect(page.getByTestId('pedido-aprovado')).toContainText(/Pedido nº \d+ realizado/);
  await expect(page.getByTestId('pedido-aprovado')).toContainText('R$ 65,88 aprovado');
});
