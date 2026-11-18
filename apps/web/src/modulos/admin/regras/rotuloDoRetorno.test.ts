import assert from 'node:assert/strict';
import test from 'node:test';
import { rotuloDoRetorno } from './rotuloDoRetorno.ts';

test('rotuloDoRetorno nomeia a consulta de origem (RF-VEN-009, RF-VEN-013, RF-CLI-005)', () => {
  assert.equal(rotuloDoRetorno('/admin/pedidos?status=APROVADA&pagina=2'), '← Voltar para os pedidos');
  assert.equal(rotuloDoRetorno('/admin/trocas?status=TROCADO&pagina=2'), '← Voltar para as trocas');
  assert.equal(rotuloDoRetorno('/admin/clientes/42?nome=ana'), '← Voltar para o cliente');
  assert.equal(rotuloDoRetorno('/admin/pedidos'), '← Voltar para os pedidos');
});

test('rotuloDoRetorno é genérico quando a origem não é conhecida', () => {
  assert.equal(rotuloDoRetorno('/admin/analise'), '← Voltar');
  assert.equal(rotuloDoRetorno(''), '← Voltar');
});
