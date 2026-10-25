import assert from 'node:assert/strict';
import test from 'node:test';
import {
  avisoDoValorDoCartao,
  cvvValido,
  emCentavos,
  montarFinalizacao,
  montarPagamento,
  problemasDosCartoes,
  valorParaApi,
  valorParaCampo,
} from './pagamentoDaCompra.ts';

test('RN-VEN-004: valor do cartão em reais, com vírgula ou ponto', () => {
  assert.equal(valorParaApi('49,88'), '49.88');
  assert.equal(valorParaApi('1.234,56'), '1234.56');
  assert.equal(valorParaApi('1.234'), '1234');
  assert.equal(valorParaApi('R$ 10,00'), '10.00');
  assert.equal(valorParaApi('59.88'), '59.88');
  assert.equal(valorParaApi('9,995'), '9.995');
  assert.equal(valorParaApi(''), null);
  assert.equal(valorParaApi('dez'), null);
  assert.equal(valorParaApi('-5'), null);
  assert.equal(valorParaCampo('49.88'), '49,88');
});

test('DEC-10: código de segurança com 3 ou 4 dígitos', () => {
  assert.ok(cvvValido('123'));
  assert.ok(cvvValido('1234'));
  assert.ok(!cvvValido('12'));
  assert.ok(!cvvValido('12a'));
});

test('RN-VEN-004: a tela aponta valor e código de segurança faltando', () => {
  assert.deepEqual(problemasDosCartoes([{ escolha: { cartaoId: 1 }, valor: '10,00', cvv: '123' }]), []);
  assert.deepEqual(problemasDosCartoes([{ escolha: { cartaoId: 1 }, valor: '', cvv: '' }]), [
    'Cartão 1: informe o valor em reais.',
    'Cartão 1: informe o código de segurança de 3 ou 4 dígitos.',
  ]);
});

test('RF-VEN-007: o pagamento vai sem o código de segurança', () => {
  const corpo = montarPagamento({ enderecoId: 1 }, 'BEMVINDO10', [2, 1], [
    { escolha: { cartaoId: 1 }, valor: '10,00', cvv: '123' },
    {
      escolha: { novo: { numero: '5555 5555 5555 4444', nomeImpresso: ' ANA ', bandeiraId: '2', validade: '2030-12' } },
      valor: '39,88',
      cvv: '456',
    },
  ]);
  assert.deepEqual(corpo, {
    entrega: { enderecoId: 1 },
    cuponsPromocionais: ['BEMVINDO10'],
    cuponsDeTroca: [2, 1],
    cartoes: [
      { cartaoId: 1, valor: '10.00' },
      { novo: { numero: '5555555555554444', nomeImpresso: 'ANA', bandeiraId: 2, validade: '2030-12' }, valor: '39.88' },
    ],
  });
  assert.equal(JSON.stringify(corpo).includes('456'), false);
  assert.deepEqual(montarPagamento({ enderecoId: 1 }, null, []).cartoes, []);
});

test('RF-VEN-008: a finalização leva a cobrança e o código de segurança de cada cartão', () => {
  const corpo = montarFinalizacao({ enderecoId: 1 }, { enderecoId: 2 }, 'BEMVINDO10', [3], [
    { escolha: { cartaoId: 1 }, valor: '49,88', cvv: ' 123 ' },
  ]);

  assert.deepEqual(corpo.entrega, { enderecoId: 1 });
  assert.deepEqual(corpo.cobranca, { enderecoId: 2 });
  assert.deepEqual(corpo.cuponsPromocionais, ['BEMVINDO10']);
  assert.deepEqual(corpo.cuponsDeTroca, [3]);
  assert.deepEqual(corpo.cartoes, [{ cartaoId: 1, valor: '49.88', codigoSeguranca: '123' }]);
});

test('emCentavos converte o decimal da API sem ponto flutuante', () => {
  assert.equal(emCentavos('49.88'), 4988);
  assert.equal(emCentavos('10.5'), 1050);
  assert.equal(emCentavos('7'), 700);
  assert.equal(emCentavos('abc'), null);
});

test('RN-VEN-004: cartão com menos de R$ 10,00 é apontado no campo', () => {
  assert.equal(avisoDoValorDoCartao('9,99', '59.88'), 'Mínimo de R$ 10,00 por cartão.');
  assert.equal(avisoDoValorDoCartao('10,00', '59.88'), null);
  assert.equal(avisoDoValorDoCartao('', '59.88'), null);
});

test('RN-VEN-004: com menos de R$ 10,00 a pagar, o cartão paga o restante', () => {
  assert.equal(avisoDoValorDoCartao('4,50', '4.50'), null);
});

test('o valor de um cartão não passa do que falta pagar', () => {
  assert.equal(avisoDoValorDoCartao('60,00', '59.88'), 'O valor passa do que falta pagar.');
});
