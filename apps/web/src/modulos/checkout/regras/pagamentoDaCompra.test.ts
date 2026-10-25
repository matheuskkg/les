import assert from 'node:assert/strict';
import test from 'node:test';
import {
  AVISO_MINIMO_POR_CARTAO,
  AVISO_UNICO_CARTAO,
  avisoDoValorDoCartao,
  cartoesAceitos,
  centavosDigitados,
  cvvValido,
  emCentavos,
  excedenteEmCentavos,
  faltaPagarEmCentavos,
  montarFinalizacao,
  montarPagamento,
  problemasDosCartoes,
  totalDigitadoEmCentavos,
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
      escolha: { novo: { numero: '5555 5555 5555 4444', nomeImpresso: ' ANA ', validade: '2030-12' } },
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
      { novo: { numero: '5555555555554444', nomeImpresso: 'ANA', validade: '2030-12' }, valor: '39.88' },
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

test('centavosDigitados lê o valor digitado e ignora vazio ou ilegível', () => {
  assert.equal(centavosDigitados('4,88'), 488);
  assert.equal(centavosDigitados('R$ 1.234,56'), 123456);
  assert.equal(centavosDigitados(''), null);
  assert.equal(centavosDigitados('dez'), null);
});

test('RN-VEN-004: cartão com menos de R$ 10,00 é apontado em qualquer posição', () => {
  assert.equal(avisoDoValorDoCartao('9,99', '59.88', false, 2), AVISO_MINIMO_POR_CARTAO);
  assert.equal(avisoDoValorDoCartao('10,00', '59.88', false, 2), null);
  assert.equal(avisoDoValorDoCartao('', '59.88', false, 2), null);

  // Review: 55,00 + 4,88 para 59,88 sem cupom: o segundo cartão não pode ficar abaixo do mínimo.
  assert.equal(avisoDoValorDoCartao('55,00', '59.88', false, 2), null);
  assert.equal(avisoDoValorDoCartao('4,88', '59.88', false, 2), AVISO_MINIMO_POR_CARTAO);
});

test('RN-VEN-004: sem cupom, restante abaixo de R$ 10,00 continua exigindo o mínimo', () => {
  assert.equal(avisoDoValorDoCartao('4,50', '4.50', false, 1), AVISO_MINIMO_POR_CARTAO);
});

test('RN-VEN-005: com cupom, restante abaixo de R$ 10,00 só aceita um cartão', () => {
  // Review: cupom, restante 8,00 e dois cartões de 4,00: a exceção não vale para dois cartões.
  assert.equal(avisoDoValorDoCartao('4,00', '8.00', true, 2), AVISO_UNICO_CARTAO);
  assert.equal(avisoDoValorDoCartao('4,00', '8.00', true, 1), null);
  assert.equal(avisoDoValorDoCartao('8,00', '8.00', true, 1), null);
});

test('o sobrepagamento é do conjunto, independente da ordem dos cartões', () => {
  assert.equal(excedenteEmCentavos('59.88', ['55,00', '4,88']), 0);
  assert.equal(excedenteEmCentavos('59.88', ['4,88', '55,00']), 0);
  assert.equal(excedenteEmCentavos('59.88', ['55,00', '10,00']), 512);
  assert.equal(excedenteEmCentavos('59.88', ['10,00', '55,00']), 512);
  assert.equal(excedenteEmCentavos('59.88', ['60,00']), 12);
});

test('a etapa Cartões só fica pronta quando o servidor aceitaria os cartões', () => {
  // Review: 55,00 + 4,88 para 59,88 sem cupom: o segundo cartão viola o mínimo de R$ 10,00.
  assert.equal(
    cartoesAceitos('59.88', false, [
      { valor: '55,00', cvv: '123' },
      { valor: '4,88', cvv: '123' },
    ]),
    false,
  );

  // Review: cupom, restante 8,00 e dois cartões de 4,00: a exceção só vale para um cartão.
  assert.equal(
    cartoesAceitos('8.00', true, [
      { valor: '4,00', cvv: '123' },
      { valor: '4,00', cvv: '123' },
    ]),
    false,
  );
  assert.equal(cartoesAceitos('8.00', true, [{ valor: '8,00', cvv: '123' }]), true);

  assert.equal(cartoesAceitos('59.88', false, [{ valor: '59,88', cvv: '123' }]), true);
  assert.equal(cartoesAceitos('4.50', false, [{ valor: '4,50', cvv: '123' }]), false);
  assert.equal(
    cartoesAceitos('59.88', false, [
      { valor: '55,00', cvv: '123' },
      { valor: '10,00', cvv: '123' },
    ]),
    false,
  );
  assert.equal(cartoesAceitos('59.88', false, [{ valor: '59,88', cvv: '12' }]), false);
  assert.equal(cartoesAceitos('0.00', false, []), true);
  assert.equal(cartoesAceitos('0.00', false, [{ valor: '10,00', cvv: '123' }]), false);
});

test('RN-VEN-004: o que falta pagar considera os valores digitados nos cartões', () => {
  assert.equal(faltaPagarEmCentavos('59.88', []), 5988);
  assert.equal(faltaPagarEmCentavos('59.88', ['10,00']), 4988);
  assert.equal(faltaPagarEmCentavos('59.88', ['10,00', '49,88']), 0);
  assert.equal(faltaPagarEmCentavos('59.88', ['60,00']), 0);
  assert.equal(faltaPagarEmCentavos('59.88', ['dez', '', '5,00']), 5488);
  assert.equal(faltaPagarEmCentavos('4.50', ['4,50']), 0);
});

test('totalDigitadoEmCentavos soma o que foi digitado e ignora o resto', () => {
  assert.equal(totalDigitadoEmCentavos(['10,00', '49,88']), 5988);
  assert.equal(totalDigitadoEmCentavos(['dez', '', '5,00']), 500);
});
