import assert from 'node:assert/strict';
import test from 'node:test';
import { rotuloDoMenuDoUsuario } from './rotuloDoMenuDoUsuario.ts';

test('o rótulo do menu identifica o usuário (ALT-16)', () => {
  assert.equal(rotuloDoMenuDoUsuario('Ana Souza'), 'Opções da conta de Ana Souza');
  assert.equal(rotuloDoMenuDoUsuario('  Maria da Silva Prado  '), 'Opções da conta de Maria da Silva Prado');
});
