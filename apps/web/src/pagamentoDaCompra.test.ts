import assert from 'node:assert/strict';
import test from 'node:test';
import {
  cvvValido,
  montarPagamento,
  problemasDosCartoes,
  valorParaApi,
  valorParaCampo,
} from './pagamentoDaCompra.ts';

test('RN-VEN-004: valor do cartão em reais, com vírgula ou ponto', () => {
  assert.equal(valorParaApi('49,88'), 49.88);
  assert.equal(valorParaApi('1.234,56'), 1234.56);
  assert.equal(valorParaApi('R$ 10,00'), 10);
  assert.equal(valorParaApi('59.88'), 59.88);
  assert.equal(valorParaApi('9,995'), 9.995);
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
      { cartaoId: 1, valor: 10 },
      { novo: { numero: '5555555555554444', nomeImpresso: 'ANA', bandeiraId: 2, validade: '2030-12' }, valor: 39.88 },
    ],
  });
  assert.equal(JSON.stringify(corpo).includes('456'), false);
  assert.deepEqual(montarPagamento({ enderecoId: 1 }, null, []).cartoes, []);
});
