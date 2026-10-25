import assert from 'node:assert/strict';
import test from 'node:test';
import { ajudaDaBandeira, identificarBandeira } from './bandeiras.ts';

test('identifica a bandeira pelo número (RN-CLI-005, DEC-18)', () => {
  assert.equal(identificarBandeira('4111111111111111'), 'Visa');
  assert.equal(identificarBandeira('5555555555554444'), 'Mastercard');
  assert.equal(identificarBandeira('6362970000457013'), 'Elo');
  assert.equal(identificarBandeira('4389350000000000'), 'Elo');
  assert.equal(identificarBandeira('378282246310005'), 'American Express');
  assert.equal(identificarBandeira('6062825624080000'), 'Hipercard');
  assert.equal(identificarBandeira('9999'), null);
  assert.equal(identificarBandeira(''), null);
});

test('aceita o número com máscara', () => {
  assert.equal(identificarBandeira('4111 1111 1111 1111'), 'Visa');
  assert.equal(identificarBandeira('5555-5555-5555-4444'), 'Mastercard');
  assert.equal(identificarBandeira('6062 8256 2408 0000'), 'Hipercard');
});

test('o prefixo mais longo vence (DEC-18)', () => {
  assert.equal(identificarBandeira('4389350000000000'), 'Elo');
  assert.equal(identificarBandeira('4312740000000000'), 'Elo');
  assert.equal(identificarBandeira('4012888888881881'), 'Visa');
  assert.equal(identificarBandeira('3841000000000000'), 'Hipercard');
});

test('ajudaDaBandeira devolve o texto exibido ao lado do número (DEC-18)', () => {
  assert.equal(ajudaDaBandeira('4111111111111111'), 'Bandeira: Visa');
  assert.equal(ajudaDaBandeira('5555 5555 5555 4444'), 'Bandeira: Mastercard');
  assert.equal(ajudaDaBandeira('9999'), undefined);
  assert.equal(ajudaDaBandeira(''), undefined);
});

test('respeita as faixas inclusivas dos prefixos', () => {
  assert.equal(identificarBandeira('5100000000000000'), 'Mastercard');
  assert.equal(identificarBandeira('5500000000000000'), 'Mastercard');
  assert.equal(identificarBandeira('2221000000000000'), 'Mastercard');
  assert.equal(identificarBandeira('2720000000000000'), 'Mastercard');
  assert.equal(identificarBandeira('5066990000000000'), 'Elo');
  assert.equal(identificarBandeira('5067780000000000'), 'Elo');
  assert.equal(identificarBandeira('5090000000000000'), 'Elo');
  assert.equal(identificarBandeira('5099990000000000'), 'Elo');
  assert.equal(identificarBandeira('6550210000000000'), 'Elo');
  assert.equal(identificarBandeira('6550580000000000'), 'Elo');
  assert.equal(identificarBandeira('2220000000000000'), null);
  assert.equal(identificarBandeira('2721000000000000'), null);
});
