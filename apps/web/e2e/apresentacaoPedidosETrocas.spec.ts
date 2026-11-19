import {
  adicionarLivro,
  concluirTroca,
  entrarComoAdmin,
  entrarComoCliente,
  expect,
  finalizarCompra,
  irParaPagamento,
  prepararPedidoEntregue,
  preencherCvv,
  rolarAte,
  selecionarCartaoSalvo,
  solicitarTroca,
  test,
} from './fixtures';

/**
 * Roteiro de apresentação do ciclo completo do pedido e da troca (atividades: RF0038 a RF0044, RN0038 a RN0043,
 * RNF0046, RF0053/RF0054 e RN0028). No DRS atual: RF-VEN-008 a RF-VEN-015, RN-VEN-008 a RN-VEN-013/016,
 * RF-EST-003/004 e RN-EST-006.
 *
 * Cada cenário começa do seed e cria o próprio pedido pela tela — Ana não tem pedidos no seed. O helper
 * `prepararPedidoEntregue` só serve de apoio nos cenários cujo assunto é a troca; o fluxo APROVADA → EM TRANSPORTE →
 * ENTREGUE é demonstrado em tela no cenário 1.
 *
 * Desvios registrados no projeto, mantidos aqui: a troca é da troca (pedido continua ENTREGUE, com indicador
 * `parcial`/`total`), e na reprovação os itens seguem reservados até o checkout expirar (DEC-12). Recusa de troca e
 * estorno não fazem parte do escopo das atividades.
 */

test('1. administrador consulta pedidos e despacha: APROVADA → EM TRANSPORTE → ENTREGUE (RF-VEN-009/010 / RF0038/0039, RN0039/0040)', async ({
  page,
}) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1, 2);
  await irParaPagamento(page);
  await selecionarCartaoSalvo(page, '1111');
  await preencherCvv(page, 1, '123');
  const numero = await finalizarCompra(page);

  await entrarComoAdmin(page);
  await page.goto('/admin/pedidos');
  await page.getByLabel('Número do pedido').fill(numero);
  await page.getByRole('button', { name: 'Filtrar' }).click();
  const linha = page
    .getByRole('row')
    .filter({ has: page.getByRole('link', { name: numero, exact: true }) });
  await rolarAte(linha);
  await expect(linha).toContainText('APROVADA');
  await linha.getByRole('link', { name: numero, exact: true }).click();

  await expect(page.getByTestId('status-do-pedido')).toHaveText('APROVADA');
  await page.getByRole('button', { name: 'Despachar' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('EM TRANSPORTE');
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');
  await expect(page.getByText('Entregue em')).toBeVisible();
});

test('2. pedido que não está ENTREGUE não oferece solicitação de troca (RF-VEN-011 / RF0040, RN0043)', async ({
  page,
}) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1);
  await irParaPagamento(page);
  await selecionarCartaoSalvo(page, '1111');
  await preencherCvv(page, 1, '123');
  await finalizarCompra(page);
  await page.getByRole('link', { name: 'Acompanhar pedido' }).click();

  // O bloqueio aparece para o usuário como ausência da opção: só pedido ENTREGUE recebe o formulário.
  await expect(page.getByTestId('status-do-pedido')).toHaveText('APROVADA');
  await expect(page.getByRole('heading', { name: 'Solicitar troca' })).toHaveCount(0);
});

test('3. cliente solicita a troca de um item de pedido ENTREGUE (RF-VEN-011 / RF0040, RN0041)', async ({ page }) => {
  const numero = await prepararPedidoEntregue(page, 2);
  await solicitarTroca(page, numero, 1); // uma das duas unidades

  const indicador = page.getByTestId('indicador-de-troca');
  await rolarAte(indicador);
  await expect(indicador).toContainText('EM TROCA');
  await expect(indicador).toContainText('parcial');
  await expect(page.getByTestId('trocas-do-pedido')).toContainText('1 × Dom Casmurro');
});

test('4. cliente solicita a troca da compra inteira (RF-VEN-011 / RF0040, RN0041)', async ({ page }) => {
  const numero = await prepararPedidoEntregue(page, 2);
  await solicitarTroca(page, numero, 2); // as duas unidades

  const indicador = page.getByTestId('indicador-de-troca');
  await rolarAte(indicador);
  await expect(indicador).toContainText('EM TROCA');
  await expect(indicador).toContainText('total');
  await expect(page.getByTestId('trocas-do-pedido')).toContainText('2 × Dom Casmurro');
});

test('5. administrador visualiza as trocas EM TROCA, autoriza e o cliente recebe a notificação (RF-VEN-012/013 / RF0041/0042, RNF0046)', async ({
  page,
}) => {
  const numero = await prepararPedidoEntregue(page, 1);
  await solicitarTroca(page, numero, 1);

  await entrarComoAdmin(page);
  await page.goto('/admin/trocas');
  await page.getByRole('group', { name: 'Marcar um ou mais status' }).getByLabel('EM TROCA').check();
  await page.getByRole('button', { name: 'Filtrar' }).click();
  const linha = page.getByRole('row').filter({ has: page.getByRole('link', { name: numero, exact: true }) });
  await rolarAte(linha);
  await expect(linha).toContainText('EM TROCA');
  await linha.getByRole('link', { name: numero, exact: true }).click();

  await page.getByRole('button', { name: 'Autorizar' }).click();
  await expect(page.getByTestId('trocas-do-pedido').getByRole('cell', { name: 'TROCA AUTORIZADA' })).toBeVisible();

  await entrarComoCliente(page);
  await page.goto('/minha-conta/notificacoes');
  const notificacao = page.getByText(/foi autorizada\. Envie os itens para a loja\./);
  await rolarAte(notificacao);
  await expect(notificacao).toBeVisible();
});

test('6. recebimento confirmado: TROCADO, cupom de troca e reentrada em estoque (RF-VEN-014/015 / RF0043/0044, RF0054, RN0042)', async ({
  page,
}) => {
  // 2 unidades compradas (estoque 20 → 18), 1 trocada com retorno ao estoque (18 → 19).
  const numero = await prepararPedidoEntregue(page, 2);
  await solicitarTroca(page, numero, 1);

  await entrarComoAdmin(page);
  await page.goto(`/admin/pedidos/${numero}`);
  await page.getByRole('button', { name: 'Autorizar' }).click();
  await expect(page.getByTestId('trocas-do-pedido').getByRole('cell', { name: 'TROCA AUTORIZADA' })).toBeVisible();
  const voltaAoEstoque = page.getByRole('group', { name: 'Voltam ao estoque' });
  await expect(voltaAoEstoque.getByRole('checkbox').first()).toBeChecked();
  await page.getByRole('button', { name: 'Confirmar recebimento' }).click();
  const tabelaDeTrocas = page.getByTestId('trocas-do-pedido');
  await expect(tabelaDeTrocas.getByRole('cell', { name: 'TROCADO' })).toBeVisible();
  await expect(tabelaDeTrocas.getByRole('cell', { name: 'R$ 47,88' })).toBeVisible();

  await page.goto('/livros/1');
  await expect(page.getByText('Em estoque: 19')).toBeVisible();

  await entrarComoCliente(page);
  await adicionarLivro(page, 1);
  await irParaPagamento(page);
  const cupons = page.getByRole('group', { name: 'Cupons de troca' });
  await expect(cupons).toContainText(/TROCA-\d+/);
  await expect(cupons).toContainText('R$ 47,88');
});

test('7. cliente usa em nova compra o cupom gerado pela troca (RF-VEN-015 / RF0044)', async ({ page }) => {
  const numero = await concluirTroca(page, 1, 1); // cupom de R$ 47,88 (preço de 1 unidade, sem frete)
  await adicionarLivro(page, 1);
  await irParaPagamento(page);

  // R$ 59,88 − R$ 47,88 = R$ 12,00 no cartão.
  const cupons = page.getByRole('group', { name: 'Cupons de troca' });
  await cupons.getByLabel(/TROCA-\d+ — R\$ 47,88/).check();
  await expect(page.getByTestId('restante')).toHaveText('R$ 12,00');
  await selecionarCartaoSalvo(page, '1111');
  await preencherCvv(page, 1, '123');
  const novoNumero = await finalizarCompra(page);
  expect(Number(novoNumero)).toBeGreaterThan(Number(numero));
});
