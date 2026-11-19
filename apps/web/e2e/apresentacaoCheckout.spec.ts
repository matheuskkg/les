import {
  conferirPagamento,
  criarCartaoSalvo,
  entrarComoCliente,
  entrarNaApi,
  esperarRestante,
  expect,
  finalizarCompra,
  irParaPagamento,
  adicionarLivro,
  preencherCvv,
  preencherNovoCartao,
  preencherNovoEndereco,
  rolarAte,
  selecionarCartaoSalvo,
  test,
} from './fixtures';

/**
 * Roteiro de apresentação da compra e do pagamento (atividades: RF0033 a RF0038, RN0031 a RN0037 e RNF0011/RNF0012).
 * No DRS atual: RF-VEN-003 a RF-VEN-008, RN-VEN-001 a RN-VEN-007 e RNF-GER-001/002.
 *
 * A finalização segue o DEC-13: o pedido é gravado EM PROCESSAMENTO numa transação, a operadora fake é chamada na
 * mesma requisição e a resposta já traz APROVADA ou REPROVADA. Por isso o E2E asserta a confirmação visível e o status
 * final — o enunciado da entrega parcial pede EM PROCESSAMENTO, que não é exposto na tela; a linha do tempo mostra
 * "Em processamento" como primeira etapa. Não se altera a regra de negócio para o teste passar.
 *
 * Fora do E2E, por não haver tela que os mostre: RN0032 (revalidação de estoque — os itens ficam reservados desde o
 * carrinho, então a mudança não pode ser provocada pela UI; coberto nos testes do server) e RNF0012 (auditoria Envers,
 * sem tela de consulta; coberta nos testes do server). A regra de um promocional por compra (RN0033) aparece no campo
 * único "Cupom promocional" e na validação do servidor.
 *
 * Conta do frete (DEC-05.3): peso = max(real, cúbico) × quantidade; blocos de 0,5 kg; frete = base da região do CEP
 * + R$ 2,00 × (blocos − 1). Os livros do seed têm o peso real dominante.
 */

test('1. cliente inicia a compra no carrinho e vê o frete calculado pelo endereço (RF-VEN-003/004/005 / RF0033/0034/0035)', async ({
  page,
}) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1); // Dom Casmurro, R$ 47,88, 0,32 kg
  await adicionarLivro(page, 5); // O Hobbit, R$ 42,90, 0,48 kg
  await irParaPagamento(page);

  // 0,32 + 0,48 = 0,80 kg → 2 blocos → base SP R$ 12,00 + R$ 2,00 = R$ 14,00; total 90,78 + 14,00.
  await expect(page.getByTestId('frete')).toHaveText('R$ 14,00');
  await expect(page.getByTestId('total')).toHaveText('R$ 104,78');
  const resumo = page.getByRole('complementary', { name: 'Resumo da compra' });
  await rolarAte(resumo);
  await expect(resumo).toContainText('Subtotal (2 itens)');
  await expect(resumo).toContainText('R$ 90,78');
});

test('2. compra aprovada com um cartão cadastrado, pedido APROVADA e baixa de estoque (RF-VEN-006/007/008 / RF0036/0037/0038, RF0053)', async ({
  page,
}) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1);
  await irParaPagamento(page);

  // 47,88 + frete SP R$ 12,00 = R$ 59,88; o cartão salvo da Ana é o Visa final 1111.
  await esperarRestante(page, 'R$ 59,88');
  await selecionarCartaoSalvo(page, '1111');
  await preencherCvv(page, 1, '123');
  await conferirPagamento(page);
  await finalizarCompra(page);

  await expect(page.getByTestId('pedido-aprovado')).toContainText('pagamento de R$ 59,88 aprovado');
  await page.getByRole('link', { name: 'Acompanhar pedido' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('APROVADA');
  await expect(page.getByText('Em processamento')).toBeVisible();

  // A baixa só acontece na aprovação (RF-EST-003 / RN-EST-006): 20 → 19 unidades.
  await page.goto('/livros/1');
  await expect(page.getByText('Em estoque: 19')).toBeVisible();
});

test('3. cliente cadastra novo endereço e novo cartão na compra e incorpora ambos ao perfil (RF-VEN-005/006 / RF0035/0036, RN-CLI-003/004/005)', async ({
  page,
}) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1);
  await irParaPagamento(page);

  await preencherNovoEndereco(page, {
    nomeCurto: 'Casa da praia',
    cep: '69005040',
    cidade: 'Manaus',
    uf: 'AM',
    salvarNoCadastro: true,
  });
  // CEP 69005040: base AM R$ 30,00 (0,32 kg → 1 bloco de 0,5 kg, sem adicional) = R$ 30,00.
  await expect(page.getByTestId('frete')).toHaveText('R$ 30,00');
  await expect(page.getByTestId('total')).toHaveText('R$ 77,88');

  // A bandeira é inferida pelo número (DEC-18); a validade é obrigatória (DEC-06).
  await preencherNovoCartao(page, {
    numero: '4012888888881881',
    nomeImpresso: 'ANA SOUZA',
    validade: '2031-06',
    cvv: '123',
    salvarNoCadastro: true,
  });
  await conferirPagamento(page);
  await finalizarCompra(page);

  await page.goto('/minha-conta/enderecos');
  await expect(page.locator('.item-da-conta').filter({ hasText: 'Casa da praia' })).toBeVisible();
  await page.goto('/minha-conta/cartoes');
  await expect(page.locator('.item-da-conta').filter({ hasText: '1881' })).toBeVisible();
});

test('4. com dois cartões, valor abaixo de R$ 10,00 é bloqueado e corrigido (RN-VEN-004 / RN0034)', async ({ page }) => {
  await entrarComoCliente(page);
  // O segundo cartão é dado de apoio: o cenário demonstra o pagamento com dois cartões, não o cadastro dele.
  await criarCartaoSalvo(page, { numero: '5555555555554444', nomeImpresso: 'ANA SOUZA', validade: '2030-01' });
  await adicionarLivro(page, 1);
  await irParaPagamento(page);
  await esperarRestante(page, 'R$ 59,88');

  const cartoes = page.getByRole('group', { name: 'Cartões' });
  const cartao1111 = cartoes.locator('.opcao-de-cartao').filter({ hasText: '1111' });
  const cartao4444 = cartoes.locator('.opcao-de-cartao').filter({ hasText: '4444' });
  await cartao1111.getByRole('checkbox').check();
  await cartao4444.getByRole('checkbox').check();

  await cartao1111.getByLabel('Valor neste cartão (R$)').fill('9,00');
  await expect(cartao1111).toContainText('Mínimo de R$ 10,00 por cartão.');

  await cartao1111.getByLabel('Valor neste cartão (R$)').fill('29,94');
  await cartao4444.getByLabel('Valor neste cartão (R$)').fill('29,94');
  await expect(cartoes).toContainText('falta pagar R$ 0,00');
  await cartao1111.getByLabel('Código de segurança').fill('123');
  await cartao4444.getByLabel('Código de segurança').fill('123');

  await conferirPagamento(page);
  const pago = page.getByRole('status').filter({ hasText: 'Pagamento conferido:' });
  await expect(pago).toContainText('1111: R$ 29,94');
  await expect(pago).toContainText('4444: R$ 29,94');
  await finalizarCompra(page);
});

test('5. promocional e cupons deixam menos de R$ 10,00 no cartão (RN-VEN-003/005 / RN0033/0035)', async ({ page }) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1);
  await irParaPagamento(page);
  await esperarRestante(page, 'R$ 59,88');

  // Um promocional por compra (RN-VEN-003): o campo é único e o botão "Retirar" permite trocá-lo.
  await page.getByLabel('Cupom promocional').fill('bemvindo10');
  await page.getByRole('button', { name: 'Aplicar cupom' }).click();
  await esperarRestante(page, 'R$ 49,88');
  await expect(page.getByRole('button', { name: 'Retirar BEMVINDO10' })).toBeVisible();

  const cupons = page.getByRole('group', { name: 'Cupons de troca' });
  await cupons.getByLabel(/TROCA-ANA-40/).check();
  await esperarRestante(page, 'R$ 9,88'); // 59,88 − 10,00 (promocional) − 40,00 (troca)

  // Com cupom e restante menor que R$ 10,00, um único cartão paga a diferença (RN-VEN-005).
  await selecionarCartaoSalvo(page, '1111');
  await expect(page.locator('#cartao-1-valor')).toHaveValue('9,88');
  await preencherCvv(page, 1, '123');
  await conferirPagamento(page);
  await expect(page.getByRole('status').filter({ hasText: 'Pagamento conferido:' })).toContainText('R$ 9,88');
  await finalizarCompra(page);
});

test('6. cupons acima do total geram cupom de troca pela diferença (RN-VEN-006 / RN0036)', async ({ page }) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1);
  await irParaPagamento(page);
  await esperarRestante(page, 'R$ 59,88');

  const cupons = page.getByRole('group', { name: 'Cupons de troca' });
  await cupons.getByLabel(/TROCA-ANA-35/).check();
  await cupons.getByLabel(/TROCA-ANA-40/).check();
  // 35 + 40 = 75,00; nenhum dos dois é desnecessário sozinho (cada um é menor que 59,88).

  const resumo = page.getByRole('complementary', { name: 'Resumo da compra' });
  await rolarAte(resumo);
  await expect(resumo).toContainText('Troco em cupom de troca');
  await expect(resumo).toContainText('R$ 15,12');
  await esperarRestante(page, 'R$ 0,00');
  await expect(page.getByText('Os cupons cobrem o total: não é preciso cartão.')).toBeVisible();

  const numero = await finalizarCompra(page);
  // O troco vira cupom de troca TROCO-<pedido>, disponível na próxima compra.
  await adicionarLivro(page, 1);
  await irParaPagamento(page);
  const novosCupons = page.getByRole('group', { name: 'Cupons de troca' });
  await expect(novosCupons).toContainText(`TROCO-${numero}`);
  await expect(novosCupons).toContainText('R$ 15,12');
});

test('7. cupom desnecessário é recusado na seleção (RN-VEN-006 / RN0036)', async ({ page }) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1);
  await irParaPagamento(page);
  await esperarRestante(page, 'R$ 59,88');

  const cupons = page.getByRole('group', { name: 'Cupons de troca' });
  await cupons.getByLabel(/TROCA-ANA-20/).check();
  await cupons.getByLabel(/TROCA-ANA-35/).check();
  await cupons.getByLabel(/TROCA-ANA-40/).check();

  // 20 + 35 + 40 = 95,00: sem o 20 os demais cobrem 75,00 e sem o 35 cobrem 60,00; só o 40 é necessário.
  const alertas = page.getByText(/é desnecessário/);
  await rolarAte(alertas.first());
  await expect(alertas).toHaveCount(2);
  await expect(alertas.nth(0)).toContainText('TROCA-ANA-20');
  await expect(alertas.nth(1)).toContainText('TROCA-ANA-35');

  await cupons.getByLabel(/TROCA-ANA-20/).uncheck();
  await cupons.getByLabel(/TROCA-ANA-35/).uncheck();
  await esperarRestante(page, 'R$ 19,88');
});

test('8. compra reprovada pela operadora não baixa estoque e mantém a compra aberta (RN-VEN-007/008 / RN0037/0038, RN0028)', async ({
  page,
  ajustarRelogio,
}) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1);
  await irParaPagamento(page);

  // Número terminado em 0000 é recusado pela operadora simulada (DEC-05.2).
  await preencherNovoCartao(page, {
    numero: '6062825624080000',
    nomeImpresso: 'ANA SOUZA',
    validade: '2032-12',
    cvv: '123',
  });
  await page.getByRole('button', { name: 'Finalizar compra' }).click();

  const reprovacao = page.getByTestId('reprovacao');
  await rolarAte(reprovacao);
  await expect(reprovacao).toContainText('Cartão final 0000: Transação recusada pela operadora');
  await expect(reprovacao).toContainText('os itens continuam reservados');

  // A compra continua aberta para nova tentativa (DEC-12).
  await page.goto('/checkout');
  await expect(page.getByRole('heading', { name: 'Pagamento', exact: true })).toBeVisible();

  // Na reprovação os itens seguem reservados até o checkout expirar, então "Em estoque: 19" ainda conta a reserva,
  // não uma baixa. Expirando o checkout (60 min), a reserva volta e o saldo disponível volta a 20.
  await ajustarRelogio(new Date(Date.now() + 61 * 60_000).toISOString());
  await page.goto('/livros/1');
  await expect(page.getByText('Em estoque: 20')).toBeVisible();
});

test('9. consulta de pedidos responde em até 1 segundo (RNF-GER-001 / RNF0011)', async ({ page }) => {
  // A Elisa tem histórico no seed, então a tabela já abre com várias linhas para a medição.
  await entrarNaApi(page, 'elisa.prado@livraria.test', 'Senha@123');
  await page.goto('/minha-conta/pedidos');
  await expect(page.locator('table tbody tr').first()).toBeVisible(); // aquecimento

  await page.goto('/minha-conta');
  const inicio = Date.now();
  await Promise.all([
    page.waitForResponse(
      (resposta) => resposta.url().includes('/api/me/pedidos?') && resposta.status() === 200,
    ),
    page.getByRole('navigation', { name: 'Seções da conta' }).getByRole('link', { name: 'Meus pedidos' }).click(),
  ]);
  expect(Date.now() - inicio).toBeLessThan(1000);
  await expect(page.getByRole('heading', { name: 'Meus pedidos' })).toBeVisible();
  await expect(page.locator('table tbody tr').first()).toBeVisible();
});
