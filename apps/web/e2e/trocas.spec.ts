import { comprarDomCasmurro, entrarComo, expect, test } from './fixtures';

const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };
const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' };

test('cliente pede a troca do pedido entregue e o admin vê a troca no pedido (RF-VEN-011, RN-VEN-013, DEC-12)', async ({
  page,
}) => {
  await entrarComo(page, ANA.login, ANA.senha);
  const numero = await comprarDomCasmurro(page, '1111', 1);
  await page.goto(`/minha-conta/pedidos/${numero}`);
  await expect(page.getByTestId('status-do-pedido')).toHaveText('APROVADA');
  await expect(page.getByRole('heading', { name: 'Solicitar troca' })).toHaveCount(0);

  await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();
  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.goto(`/admin/pedidos/${numero}`);
  await page.getByRole('button', { name: 'Despachar' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('EM TRANSPORTE');
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');

  await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();
  await entrarComo(page, ANA.login, ANA.senha);
  await page.goto(`/minha-conta/pedidos/${numero}`);
  await expect(page.getByText('Nenhuma troca solicitada.')).toBeVisible();
  await page.getByRole('button', { name: 'Solicitar troca' }).click();
  await expect(page.getByRole('alert')).toHaveText('Informe a quantidade de pelo menos um item para trocar.');
  await page.getByLabel('Dom Casmurro (até 1)').fill('1');
  await page.getByRole('button', { name: 'Solicitar troca' }).click();

  // O pedido continua ENTREGUE; a troca aparece no indicador e o formulário some sem unidades disponíveis.
  await expect(page.getByRole('status')).toHaveText('Troca solicitada. Avisaremos quando ela for analisada.');
  await expect(page.getByTestId('indicador-de-troca')).toHaveText('EM TROCA (total)');
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');
  await expect(page.getByTestId('trocas-do-pedido')).toContainText('1 × Dom Casmurro');
  await expect(page.getByRole('heading', { name: 'Solicitar troca' })).toHaveCount(0);

  await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();
  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.goto(`/admin/pedidos/${numero}`);
  await expect(page.getByTestId('indicador-de-troca')).toHaveText('EM TROCA (total)');
  await expect(page.getByTestId('trocas-do-pedido')).toContainText('EM TROCA');
});
