import { entrarComo, expect, test } from './fixtures';

// Com o relógio em 2027-10-01, a janela de 365 dias só pega o fim do seed histórico: Elisa tem 5 pedidos somando
// R$ 685,69, o último há 346 dias, e ranking 68 (RN-CLI-007, DEC-04).
test.beforeEach(async ({ ajustarRelogio }) => {
  await ajustarRelogio('2027-10-01T12:00:00-03:00');
});

test('cliente vê o próprio ranking nos dados da conta (RN-CLI-007)', async ({ page }) => {
  await entrarComo(page, 'elisa.prado@livraria.test', 'Senha@123');
  await page.goto('/minha-conta/dados');
  await expect(page.getByTestId('ranking')).toHaveText('68');
});

test('admin vê o ranking na consulta e no detalhe do cliente (RN-CLI-007, DEC-15.4)', async ({ page }) => {
  await entrarComo(page, 'admin@livraria.test', 'Admin@123');
  await page.goto('/admin/clientes?cpf=39053344705');
  const linha = page.getByRole('row', { name: /Elisa Prado/ });
  await expect(linha.getByRole('cell').last()).toHaveText('68');

  await page.goto('/admin/clientes/5');
  await expect(page.getByText('68 de 100')).toBeVisible();
});
