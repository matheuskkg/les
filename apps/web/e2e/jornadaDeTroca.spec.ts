import type { Page } from '@playwright/test';
import { adicionarDomCasmurro, comprarDomCasmurro, entrarComo, expect, sair, test } from './fixtures';

// A Ana compra um Dom Casmurro (R$ 47,88 + frete de SP R$ 12,00) com o Visa final 1111 (cartão 1).
const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };
const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' };

async function estoqueDoDomCasmurro(page: Page, esperado: number): Promise<void> {
  await page.goto('/livros/1');
  await expect(page.getByText(`Em estoque: ${esperado}`)).toBeVisible();
}

test('jornada de troca: entregue, trocado com retorno ao estoque e cupom usado na compra seguinte (E2E-04, RF-VEN-011 a RF-VEN-015)', async ({
  page,
}) => {
  test.setTimeout(90_000);

  await entrarComo(page, ANA.login, ANA.senha);
  const numero = await comprarDomCasmurro(page, '1111', 1);
  await estoqueDoDomCasmurro(page, 19);
  await sair(page);

  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.goto(`/admin/pedidos/${numero}`);
  await page.getByRole('button', { name: 'Despachar' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('EM TRANSPORTE');
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');
  await sair(page);

  // RF-VEN-011: a cliente pede a troca do pedido entregue (EM TROCA).
  await entrarComo(page, ANA.login, ANA.senha);
  await page.goto(`/minha-conta/pedidos/${numero}`);
  await page.getByLabel('Dom Casmurro (até 1)').fill('1');
  await page.getByRole('button', { name: 'Solicitar troca' }).click();
  await expect(page.getByTestId('indicador-de-troca')).toHaveText('EM TROCA (total)');
  await sair(page);

  // RF-VEN-012 / RN-VEN-016: o admin autoriza; RF-VEN-014: recebe com retorno ao estoque (TROCADO).
  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.goto(`/admin/pedidos/${numero}`);
  await page.getByRole('button', { name: 'Autorizar' }).click();
  await expect(page.getByTestId('indicador-de-troca')).toHaveText('TROCA AUTORIZADA (total)');
  await expect(page.getByRole('group', { name: 'Voltam ao estoque' }).getByLabel('Dom Casmurro')).toBeChecked();
  await page.getByRole('button', { name: 'Confirmar recebimento' }).click();
  await expect(page.getByTestId('indicador-de-troca')).toHaveText('TROCADO (total)');
  await sair(page);

  // A cliente foi avisada da autorização e recebeu o cupom de troca (RF-VEN-015).
  await entrarComo(page, ANA.login, ANA.senha);
  await estoqueDoDomCasmurro(page, 20);
  await page.goto('/minha-conta/notificacoes');
  await expect(page.getByText(/foi autorizada/)).toBeVisible();
  const conclusao = page.getByText(/foi concluída\. O cupom de troca TROCA-\d+, de R\$ 47,88/);
  await expect(conclusao).toBeVisible();
  const cupom = /TROCA-\d+/.exec((await conclusao.textContent()) ?? '')?.[0] ?? '';
  expect(cupom, 'código do cupom de troca').toBeTruthy();

  // Nova compra: o cupom de troca abate o livro e o cartão paga só o frete (RF-VEN-007, RN-VEN-005).
  await adicionarDomCasmurro(page);
  await page.goto('/carrinho');
  await page.getByRole('button', { name: 'Ir para pagamento' }).click();
  await expect(page.getByTestId('restante')).toHaveText('R$ 59,88');
  await page.getByRole('group', { name: 'Cupons de troca' }).getByLabel(new RegExp(cupom)).check();
  await expect(page.getByTestId('restante')).toHaveText('R$ 12,00');
  await page.getByRole('group', { name: 'Cartões' }).getByLabel(/1111/).check();
  await expect(page.locator('#cartao-1-valor')).toHaveValue('12,00');
  await page.locator('#cartao-1-cvv').fill('123');
  await page.getByRole('button', { name: 'Finalizar compra' }).click();
  await expect(page.getByTestId('pedido-aprovado')).toContainText(/Pedido nº \d+ realizado/);

  // O cupom foi usado e não aparece mais para a próxima compra.
  await adicionarDomCasmurro(page);
  await page.goto('/carrinho');
  await page.getByRole('button', { name: 'Ir para pagamento' }).click();
  await expect(page.getByTestId('restante')).toHaveText('R$ 59,88');
  await expect(page.getByRole('group', { name: 'Cupons de troca' }).getByLabel(new RegExp(cupom))).toHaveCount(0);
});
