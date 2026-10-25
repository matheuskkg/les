import { entrarComo, expect, numeroDoPedidoAprovado, sair, test } from './fixtures';

// A Ana compra dois Dom Casmurro (R$ 47,88 cada) para o endereço "Trabalho", com cobrança em "Casa". O Visa final
// 1111 é o cartão salvo dela; o terminado em 0000 é sempre recusado pela operadora simulada (DEC-05.2).
const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

test('jornada de compra: reprovada, aprovada com dois cartões e cupom, despachada e entregue (E2E-03, RF-VEN-001 a RF-VEN-010)', async ({
  page,
}) => {
  test.setTimeout(90_000);

  await entrarComo(page, ANA.login, ANA.senha);

  // Vitrine → livro → carrinho, alterando a quantidade (RF-VEN-001, RF-VEN-002).
  await page.goto('/');
  await page.getByRole('link', { name: /Dom Casmurro/ }).first().click();
  await expect(page.getByText('Em estoque: 20')).toBeVisible();
  await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
  await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();
  await page.getByRole('banner').getByRole('link', { name: 'Ver carrinho' }).click();
  const quantidade = page.locator('.item-da-conta', { hasText: 'Dom Casmurro' }).getByLabel('Quantidade');
  await quantidade.fill('2');
  await quantidade.press('Enter');
  await expect(page.getByText('R$ 95,76').first()).toBeVisible();

  // Checkout com endereços de entrega e cobrança diferentes e o frete calculado (RF-VEN-004, RF-VEN-005).
  await page.getByRole('button', { name: 'Ir para pagamento' }).click();
  await expect(page).toHaveURL(/\/checkout$/);
  await page.getByRole('group', { name: 'Endereço de entrega' }).getByLabel(/Trabalho/).check();
  await page.getByLabel('Cobrança no mesmo endereço da entrega').uncheck();
  await page.getByRole('group', { name: 'Endereço de cobrança' }).getByLabel(/Casa/).check();
  await expect(page.getByTestId('frete')).toHaveText('R$ 14,00');
  await expect(page.getByTestId('restante')).toHaveText('R$ 109,76');

  // Cartão novo terminado em 0000: REPROVADA, e a compra continua aberta (RN-VEN-007, RN-VEN-008).
  const cartoes = page.getByRole('group', { name: 'Cartões' });
  await cartoes.getByLabel('Outro cartão').check();
  await page.locator('#cartao-novo-numero').fill('6062825624080000');
  await page.locator('#cartao-novo-nome').fill('ANA SOUZA');
  await page.locator('#cartao-novo-validade').fill('2032-12');
  await page.locator('#cartao-novo-cvv').fill('123');
  await page.getByRole('button', { name: 'Finalizar compra' }).click();
  await expect(page.getByTestId('reprovacao')).toContainText('Cartão final 0000: Transação recusada pela operadora');
  await expect(page).toHaveURL(/\/checkout$/);

  // Nova tentativa: cupom promocional e dois cartões, o salvo com o código de segurança (RF-VEN-006, RF-VEN-007).
  await page.getByLabel('Cupom promocional').fill('bemvindo10');
  await page.getByRole('button', { name: 'Aplicar cupom' }).click();
  await expect(page.getByTestId('restante')).toHaveText('R$ 99,76');

  await page.locator('#cartao-novo-numero').fill('4012888888881881');
  await page.locator('#cartao-novo-validade').fill('2031-06');
  await page.locator('#cartao-novo-cvv').fill('321');
  await cartoes.getByLabel(/1111/).check();
  await page.locator('#cartao-1-valor').fill('50,00');
  await page.locator('#cartao-1-cvv').fill('123');
  await page.locator('#cartao-novo-valor').fill('49,76');
  await page.getByRole('button', { name: 'Finalizar compra' }).click();

  const numero = await numeroDoPedidoAprovado(page);

  // APROVADA: estoque baixado (RF-EST-003) e cliente notificada (DEC-13).
  await page.goto('/livros/1');
  await expect(page.getByText('Em estoque: 18')).toBeVisible();
  await page.getByRole('banner').getByRole('link', { name: /Notificações/ }).click();
  await expect(page.getByText(new RegExp(`${numero}.*aprovado|aprovado.*${numero}`, 'i')).first()).toBeVisible();

  // O administrador despacha e confirma a entrega (RF-VEN-009, RF-VEN-010).
  await sair(page);
  await entrarComo(page, 'admin@livraria.test', 'Admin@123');
  await page.goto(`/admin/pedidos/${numero}`);
  await expect(page.getByTestId('status-do-pedido')).toHaveText('APROVADA');
  await expect(page.getByTestId('total-do-pedido')).toHaveText('R$ 99,76');
  await page.getByRole('button', { name: 'Despachar' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('EM TRANSPORTE');
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');

  // A cliente vê o pedido entregue em "Meus pedidos" (RF-CLI-005).
  await sair(page);
  await entrarComo(page, ANA.login, ANA.senha);
  await page.goto('/minha-conta/pedidos');
  const linha = page.getByRole('row').filter({ has: page.getByRole('link', { name: numero, exact: true }) });
  await expect(linha).toContainText('ENTREGUE');
  await expect(linha).toContainText('R$ 99,76');
});
