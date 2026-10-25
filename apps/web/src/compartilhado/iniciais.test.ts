import assert from 'node:assert/strict';
import test from 'node:test';
import { iniciais } from './iniciais.ts';

test('iniciais usa o primeiro e o último nome', () => {
  assert.equal(iniciais('Ana Souza'), 'AS');
  assert.equal(iniciais('Maria da Silva Prado'), 'MP');
  assert.equal(iniciais('Administração'), 'A');
  assert.equal(iniciais('  '), '?');
});
