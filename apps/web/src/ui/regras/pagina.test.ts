import assert from 'node:assert/strict';
import test from 'node:test';
import { apenasDigitos, paginaDaEntrada } from './pagina.ts';

test('RNF-GER-001: a máscara do campo de página aceita apenas dígitos, inclusive colados', () => {
  assert.equal(apenasDigitos('12'), '12');
  assert.equal(apenasDigitos('007'), '007');
  assert.equal(apenasDigitos('1a2b3'), '123');
  assert.equal(apenasDigitos(' 4 5 '), '45');
  assert.equal(apenasDigitos('abc'), '');
  assert.equal(apenasDigitos(''), '');
  // Colar do clipboard traz letras, espaços, pontuação e até quebras de linha.
  assert.equal(apenasDigitos('Página 12 / 50\n'), '1250');
});

test('RNF-GER-001: a sanitização é idempotente quando o valor já é igual ao estado', () => {
  // Campo mostrando "7" (estado "7"): digitar "a" gera "7a", cuja máscara volta a "7".
  // O componente reescreve o valor no nó do input porque o setState do Preact 11 faz bail-out quando o valor não muda;
  // sem jsdom/testing-library na lista do DEC-01, aqui se testa só a regra pura, não o re-render.
  const estado = '7';
  const sanitizado = apenasDigitos('7a');
  assert.equal(sanitizado, estado);
  assert.equal(apenasDigitos(sanitizado), estado);
});

test('RNF-GER-001: campo vazio não navega', () => {
  assert.equal(paginaDaEntrada('', 8), null);
  assert.equal(paginaDaEntrada('   ', 8), null);
  assert.equal(paginaDaEntrada('abc', 8), null);
});

test('RNF-GER-001: zeros à esquerda e zero são normalizados', () => {
  assert.equal(paginaDaEntrada('007', 12), 7);
  assert.equal(paginaDaEntrada('0001', 12), 1);
  assert.equal(paginaDaEntrada('0', 12), 1);
  assert.equal(paginaDaEntrada('00', 12), 1);
});

test('RNF-GER-001: página acima do total vai para a última página', () => {
  assert.equal(paginaDaEntrada('99', 12), 12);
  assert.equal(paginaDaEntrada('13', 12), 12);
  assert.equal(paginaDaEntrada('12', 12), 12);
  assert.equal(paginaDaEntrada('999999999999999999999999', 12), 12);
});

test('RNF-GER-001: páginas dentro do intervalo são mantidas', () => {
  assert.equal(paginaDaEntrada('1', 12), 1);
  assert.equal(paginaDaEntrada('3', 12), 3);
  assert.equal(paginaDaEntrada('12', 50), 12);
});

test('RNF-GER-001: total inválido não quebra a normalização', () => {
  assert.equal(paginaDaEntrada('5', 0), 1);
  assert.equal(paginaDaEntrada('5', -3), 1);
  assert.equal(paginaDaEntrada('5', Number.NaN), 1);
});
