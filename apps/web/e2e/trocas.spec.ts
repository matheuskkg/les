import type { Page } from '@playwright/test';
import { comprarDomCasmurro, entrarComo, expect, test } from './fixtures';

const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };
const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' };

async function sair(page: Page): Promise<void> {
  await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();
}

/** A Ana compra um Dom Casmurro, o admin despacha e confirma a entrega, e a Ana volta logada. */
async function pedidoEntregueDaAna(page: Page): Promise<string> {
  await entrarComo(page, ANA.login, ANA.senha);
  const numero = await comprarDomCasmurro(page, '1111', 1);
  await sair(page);
  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.goto(`/admin/pedidos/${numero}`);
  await page.getByRole('button', { name: 'Despachar' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('EM TRANSPORTE');
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');
  await sair(page);
  await entrarComo(page, ANA.login, ANA.senha);
  return numero;
}

async function pedirTrocaDoDomCasmurro(page: Page, numero: string): Promise<void> {
  await page.goto(`/minha-conta/pedidos/${numero}`);
  await page.getByLabel('Dom Casmurro (até 1)').fill('1');
  await page.getByRole('button', { name: 'Solicitar troca' }).click();
  await expect(page.getByRole('status')).toHaveText('Troca solicitada. Avisaremos quando ela for analisada.');
}

test('cliente pede a troca do pedido entregue e o admin vê a troca no pedido (RF-VEN-011, RN-VEN-013, DEC-12)', async ({
  page,
}) => {
  await entrarComo(page, ANA.login, ANA.senha);
  const numero = await comprarDomCasmurro(page, '1111', 1);
  await page.goto(`/minha-conta/pedidos/${numero}`);
  await expect(page.getByTestId('status-do-pedido')).toHaveText('APROVADA');
  await expect(page.getByRole('heading', { name: 'Solicitar troca' })).toHaveCount(0);
  await sair(page);

  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.goto(`/admin/pedidos/${numero}`);
  await page.getByRole('button', { name: 'Despachar' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('EM TRANSPORTE');
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');
  await sair(page);

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

  await sair(page);
  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.goto(`/admin/pedidos/${numero}`);
  await expect(page.getByTestId('indicador-de-troca')).toHaveText('EM TROCA (total)');
  await expect(page.getByTestId('trocas-do-pedido')).toContainText('EM TROCA');
});

test('admin encontra a troca, recusa com motivo e o cliente vê a recusa (RF-VEN-013, DEC-12, DEC-13)', async ({
  page,
}) => {
  const numero = await pedidoEntregueDaAna(page);
  await pedirTrocaDoDomCasmurro(page, numero);
  await sair(page);

  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.getByRole('navigation', { name: 'Seções da administração' }).getByRole('link', { name: 'Trocas' })
    .click();
  await expect(page.getByRole('heading', { name: 'Trocas' })).toBeVisible();
  const linha = page.getByRole('row', { name: /Ana Souza/ });
  await expect(linha).toContainText('EM TROCA');
  await linha.getByRole('link', { name: numero }).click();

  await page.getByRole('button', { name: 'Recusar' }).click();
  await page.getByRole('button', { name: 'Confirmar recusa' }).click();
  await expect(page.getByRole('alert')).toHaveText('Informe o motivo da recusa da troca.');
  await page.getByLabel('Motivo da recusa').fill('Livro com marcas de uso');
  await page.getByRole('button', { name: 'Confirmar recusa' }).click();
  await expect(page.getByTestId('indicador-de-troca')).toHaveText('TROCA RECUSADA (parcial)');
  await expect(page.getByTestId('trocas-do-pedido')).toContainText('Motivo: Livro com marcas de uso');
  await expect(page.getByRole('button', { name: 'Recusar' })).toHaveCount(0);

  await page.goto('/admin/trocas');
  await expect(page.getByText('Nenhuma troca encontrada.')).toBeVisible();
  await page.getByLabel('Status').selectOption('TROCA_RECUSADA');
  await expect(page).toHaveURL(/status=TROCA_RECUSADA/);
  await expect(page.getByRole('row', { name: /Ana Souza/ })).toContainText('TROCA RECUSADA');

  // DEC-12: o item recusado não pode ser pedido de novo.
  await sair(page);
  await entrarComo(page, ANA.login, ANA.senha);
  await page.goto(`/minha-conta/pedidos/${numero}`);
  await expect(page.getByTestId('indicador-de-troca')).toHaveText('TROCA RECUSADA (parcial)');
  await expect(page.getByRole('heading', { name: 'Solicitar troca' })).toHaveCount(0);
  await page.goto('/minha-conta/notificacoes');
  await expect(page.getByText(/foi recusada\. Motivo: Livro com marcas de uso/)).toBeVisible();
});

test('admin autoriza a troca e o pedido aparece pela situação da troca (RF-VEN-012, RN-VEN-016, DEC-12)', async ({
  page,
}) => {
  const numero = await pedidoEntregueDaAna(page);
  await pedirTrocaDoDomCasmurro(page, numero);
  await sair(page);

  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.goto(`/admin/pedidos/${numero}`);
  await page.getByRole('button', { name: 'Autorizar' }).click();
  await expect(page.getByTestId('indicador-de-troca')).toHaveText('TROCA AUTORIZADA (total)');
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');

  await page.goto('/admin/pedidos');
  await page.getByRole('group', { name: 'Situação da troca' }).getByLabel('TROCA AUTORIZADA').check();
  await page.getByRole('button', { name: 'Filtrar' }).click();
  await expect(page).toHaveURL(/situacao=TROCA_AUTORIZADA/);
  await expect(page.getByRole('row', { name: new RegExp(numero) })).toBeVisible();

  await sair(page);
  await entrarComo(page, ANA.login, ANA.senha);
  await page.goto('/minha-conta/notificacoes');
  await expect(page.getByText(/foi autorizada/)).toBeVisible();
});
