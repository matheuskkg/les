import { comprarDomCasmurro, entrarComo, expect, test } from './fixtures';

const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

test('cliente consulta as próprias transações e o admin vê o histórico no cadastro (RF-CLI-005)', async ({
  page,
}) => {
  await entrarComo(page, ANA.login, ANA.senha);
  await page.goto('/minha-conta/pedidos');
  await expect(page.getByText('Você ainda não fez nenhum pedido.')).toBeVisible();

  const numero = await comprarDomCasmurro(page, '1111', 1);

  await page.goto('/minha-conta');
  await page.getByRole('navigation', { name: 'Seções da conta' }).getByRole('link', { name: 'Meus pedidos' }).click();
  const linha = page.getByRole('row', { name: new RegExp(numero) });
  await expect(linha).toContainText('APROVADA');
  await expect(linha).toContainText('R$ 59,88');
  await linha.getByRole('link', { name: numero }).click();
  await expect(page.getByRole('heading', { name: `Pedido ${numero}` })).toBeVisible();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('APROVADA');
  await expect(page.getByRole('cell', { name: /Dom Casmurro/ })).toBeVisible();
  await expect(page.getByTestId('total-do-pedido')).toHaveText('R$ 59,88');

  await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();
  await entrarComo(page, 'admin@livraria.test', 'Admin@123');
  await page.goto('/admin/clientes/1');
  await expect(page.getByRole('heading', { name: 'Pedidos' })).toBeVisible();
  await expect(page.getByRole('row', { name: new RegExp(numero) })).toContainText('APROVADA');
});
