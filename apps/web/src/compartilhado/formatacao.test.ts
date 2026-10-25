import assert from 'node:assert/strict';
import test from 'node:test';
import {
  tomDoStatus,
  formatarCep,
  formatarCpf,
  formatarData,
  formatarDecimal,
  formatarDinheiro,
  textoDoStatus,
} from './formatacao.ts';

test('formatarDinheiro formata milhares e centavos', () => {
  assert.equal(formatarDinheiro('1234.56'), 'R$ 1.234,56');
  assert.equal(formatarDinheiro('999999.9'), 'R$ 999.999,90');
  assert.equal(formatarDinheiro('0'), 'R$ 0,00');
  assert.equal(formatarDinheiro('12'), 'R$ 12,00');
  assert.equal(formatarDinheiro('12.'), 'R$ 12,00');
  assert.equal(formatarDinheiro('0005.5'), 'R$ 5,50');
});

test('formatarDinheiro arredonda HALF_UP na terceira casa (DEC-15.1)', () => {
  assert.equal(formatarDinheiro('38.3295'), 'R$ 38,33');
  assert.equal(formatarDinheiro('0.995'), 'R$ 1,00');
  assert.equal(formatarDinheiro('999.995'), 'R$ 1.000,00');
  assert.equal(formatarDinheiro('0.005'), 'R$ 0,01');
  assert.equal(formatarDinheiro('1.999'), 'R$ 2,00');
  assert.equal(formatarDinheiro('1.0049'), 'R$ 1,00');
  assert.equal(formatarDinheiro('1.005'), 'R$ 1,01');
});

test('formatarDinheiro trata valores negativos', () => {
  assert.equal(formatarDinheiro('-1.005'), '-R$ 1,01');
  assert.equal(formatarDinheiro('-1234.56'), '-R$ 1.234,56');
  assert.equal(formatarDinheiro('-0.994'), '-R$ 0,99');
  assert.equal(formatarDinheiro('-0.995'), '-R$ 1,00');
});

test('formatarDinheiro devolve a entrada inalterada quando inválida', () => {
  assert.equal(formatarDinheiro('abc'), 'abc');
  assert.equal(formatarDinheiro('1,50'), '1,50');
  assert.equal(formatarDinheiro('1e3'), '1e3');
  assert.equal(formatarDinheiro('  '), '  ');
  assert.equal(formatarDinheiro(''), '');
});

test('formatarDecimal usa vírgula pt-BR em números e strings', () => {
  assert.equal(formatarDecimal(21), '21');
  assert.equal(formatarDecimal(21.5), '21,5');
  assert.equal(formatarDecimal(0.35), '0,35');
  assert.equal(formatarDecimal('12.50'), '12,50');
  assert.equal(formatarDecimal(' 1.2 '), '1,2');
  assert.equal(formatarDecimal(''), '');
});

test('formatarData converte ISO sem passar por fuso', () => {
  assert.equal(formatarData('2026-10-21'), '21/10/2026');
  assert.equal(formatarData('2026-10-21T23:59:00Z'), '21/10/2026');
  assert.equal(formatarData('inválida'), 'inválida');
  assert.equal(formatarData('2026-1-2'), '2026-1-2');
});

test('textoDoStatus troca underscores por espaços em maiúsculas', () => {
  assert.equal(textoDoStatus('EM_PROCESSAMENTO'), 'EM PROCESSAMENTO');
  assert.equal(textoDoStatus('ENTREGUE'), 'ENTREGUE');
  assert.equal(textoDoStatus(' em_troca '), 'EM TROCA');
});

test('formatarCpf exibe só CPF de 11 dígitos formatado', () => {
  assert.equal(formatarCpf('52998224725'), '529.982.247-25');
  assert.equal(formatarCpf('123'), '123');
});

test('formatarCep exibe só CEP de 8 dígitos formatado (RF-CLI-001)', () => {
  assert.equal(formatarCep('69005040'), '69005-040');
  assert.equal(formatarCep('69005-040'), '69005-040');
  assert.equal(formatarCep('6900504'), '6900504');
  assert.equal(formatarCep('690050400'), '690050400');
});

test('tomDoStatus aceita o enum ou o texto do DRS', () => {
  assert.equal(tomDoStatus('APROVADA'), 'positivo');
  assert.equal(tomDoStatus('EM TRANSPORTE'), 'atencao');
  assert.equal(tomDoStatus('TROCA_RECUSADA'), 'critico');
  assert.equal(tomDoStatus('desconhecido'), 'neutro');
});
