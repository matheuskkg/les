import assert from 'node:assert/strict';
import test from 'node:test';
import { lerIntencao, serializarIntencao } from './intencaoDeCompra.ts';

test('a intenção de compra volta igual depois de guardada (RF-VEN-001)', () => {
  assert.deepEqual(lerIntencao(serializarIntencao({ livroId: 7, quantidade: 2 })), { livroId: 7, quantidade: 2 });
});

test('intenção ausente, malformada ou com números inválidos é descartada', () => {
  assert.equal(lerIntencao(null), null);
  assert.equal(lerIntencao('{'), null);
  assert.equal(lerIntencao('"texto"'), null);
  assert.equal(lerIntencao('{"livroId":0,"quantidade":1}'), null);
  assert.equal(lerIntencao('{"livroId":1,"quantidade":1.5}'), null);
});
