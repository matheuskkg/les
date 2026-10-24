import { entrarComo, expect, test } from './fixtures';

// Um Dom Casmurro (R$ 47,88) para o primeiro endereço da Ana, em SP (frete R$ 12,00), pago com o Visa final 1111.
const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

test('admin encontra o pedido aprovado pelos filtros, despacha e confirma a entrega (RF-VEN-009, RF-VEN-010, DEC-12)', async ({
  page,
}) => {
  await entrarComo(page, ANA.login, ANA.senha);
  await page.goto('/livros/1');
  await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
  await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();
  await page.goto('/carrinho');
  await page.getByRole('button', { name: 'Ir para pagamento' }).click();
  await expect(page.getByTestId('restante')).toHaveText('R$ 59,88');
  await page.getByRole('group', { name: 'Cartões' }).getByLabel(/1111/).check();
  await page.locator('#cartao-1-cvv').fill('123');
  await page.getByRole('button', { name: 'Finalizar compra' }).click();
  const aprovado = page.getByTestId('pedido-aprovado');
  await expect(aprovado).toContainText(/Pedido nº \d+ realizado/);
  const numero = /Pedido nº (\d+)/.exec((await aprovado.textContent()) ?? '')?.[1] ?? '';

  await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();
  await entrarComo(page, 'admin@livraria.test', 'Admin@123');
  await page.goto('/admin/pedidos');
  await expect(page.getByRole('heading', { name: 'Pedidos' })).toBeVisible();
  await expect(page.getByText('1 pedido encontrado')).toBeVisible();

  await page.getByLabel('CPF').fill('111.444.777-35');
  await page.getByRole('button', { name: 'Filtrar' }).click();
  await expect(page).toHaveURL(/cpf=111.444.777-35/);
  await expect(page.getByText('Nenhum pedido encontrado.')).toBeVisible();

  await page.getByLabel('CPF').fill('529.982.247-25');
  await page.getByRole('group', { name: 'Status' }).getByLabel('APROVADA').check();
  await page.getByRole('button', { name: 'Filtrar' }).click();
  await expect(page).toHaveURL(/status=APROVADA/);
  const linha = page.getByRole('row', { name: new RegExp(numero) });
  await expect(linha).toContainText('Ana Souza');
  await expect(linha).toContainText('APROVADA');
  await expect(linha).toContainText('R$ 59,88');

  await linha.getByRole('link', { name: numero }).click();
  await expect(page.getByRole('heading', { name: `Pedido ${numero}` })).toBeVisible();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('APROVADA');
  await expect(page.getByRole('cell', { name: /Dom Casmurro/ })).toBeVisible();
  await expect(page.getByText('Visa final 1111: R$ 59,88')).toBeVisible();
  await expect(page.getByTestId('total-do-pedido')).toHaveText('R$ 59,88');

  // RN-VEN-009 e RN-VEN-010: APROVADA → EM TRANSPORTE → ENTREGUE, sem ação depois da entrega.
  await page.getByRole('button', { name: 'Despachar' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('EM TRANSPORTE');
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');
  await expect(page.getByText('Entregue em')).toBeVisible();
  await expect(page.getByRole('button', { name: /Despachar|Confirmar entrega/ })).toHaveCount(0);
});
