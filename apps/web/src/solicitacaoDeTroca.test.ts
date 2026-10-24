import assert from 'node:assert/strict';
import { test } from 'node:test';
import { itensDaSolicitacao } from './solicitacaoDeTroca.ts';

test('RF-VEN-011: só os itens com quantidade preenchida vão na solicitação', () => {
  assert.deepEqual(itensDaSolicitacao({ 101: '2', 102: '', 103: '0', 104: ' 1 ' }), [
    { itemPedidoId: 101, quantidade: 2 },
    { itemPedidoId: 104, quantidade: 1 },
  ]);
  assert.deepEqual(itensDaSolicitacao({ 101: '1,5' }), [{ itemPedidoId: 101, quantidade: 0 }]);
});
