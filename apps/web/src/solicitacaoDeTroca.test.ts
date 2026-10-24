import assert from 'node:assert/strict';
import { test } from 'node:test';
import type { components } from './api/esquema';
import { hojeNaLoja, itensDaSolicitacao, podeSolicitarTroca } from './solicitacaoDeTroca.ts';

type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];

function pedido(parcial: Partial<PedidoDetalhe>, disponivel = 1): PedidoDetalhe {
  return {
    status: 'ENTREGUE',
    trocaAte: '2026-10-31',
    itens: [{ disponivelParaTroca: disponivel }],
    ...parcial,
  } as PedidoDetalhe;
}

test('RN-VEN-013: hoje é a data no fuso da loja', () => {
  assert.equal(hojeNaLoja(new Date('2026-11-01T02:30:00Z')), '2026-10-31');
  assert.equal(hojeNaLoja(new Date('2026-11-01T03:00:00Z')), '2026-11-01');
});

test('RF-VEN-011: troca só de pedido entregue, no prazo e com unidade disponível', () => {
  assert.equal(podeSolicitarTroca(pedido({}), '2026-10-31'), true);
  assert.equal(podeSolicitarTroca(pedido({}), '2026-11-01'), false);
  assert.equal(podeSolicitarTroca(pedido({ status: 'EM_TRANSPORTE', trocaAte: undefined }), '2026-10-24'), false);
  assert.equal(podeSolicitarTroca(pedido({}, 0), '2026-10-24'), false);
});

test('RF-VEN-011: só os itens com quantidade preenchida vão na solicitação', () => {
  assert.deepEqual(itensDaSolicitacao({ 101: '2', 102: '', 103: '0', 104: ' 1 ' }), [
    { itemPedidoId: 101, quantidade: 2 },
    { itemPedidoId: 104, quantidade: 1 },
  ]);
  assert.deepEqual(itensDaSolicitacao({ 101: '1,5' }), [{ itemPedidoId: 101, quantidade: 0 }]);
});
