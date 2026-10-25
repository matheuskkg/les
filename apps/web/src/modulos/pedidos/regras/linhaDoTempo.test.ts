import assert from 'node:assert/strict';
import test from 'node:test';
import { etapasDoPedido } from './linhaDoTempo.ts';

const situacoes = (status: Parameters<typeof etapasDoPedido>[0]) =>
  etapasDoPedido(status).map((etapa) => etapa.situacao);

test('RF-VEN-009: o pedido em transporte já passou pelo pagamento', () => {
  assert.deepEqual(situacoes('EM_TRANSPORTE'), ['concluida', 'concluida', 'atual', 'pendente']);
});

test('RF-VEN-010: o pedido entregue conclui todas as etapas', () => {
  assert.deepEqual(situacoes('ENTREGUE'), ['concluida', 'concluida', 'concluida', 'concluida']);
});

test('RN-VEN-008: o pedido reprovado para no pagamento', () => {
  assert.deepEqual(
    etapasDoPedido('REPROVADA').map((etapa) => [etapa.rotulo, etapa.situacao]),
    [
      ['Em processamento', 'concluida'],
      ['Pagamento reprovado', 'falhou'],
    ],
  );
});

test('o pedido recém-feito começa em processamento', () => {
  assert.deepEqual(situacoes('EM_PROCESSAMENTO'), ['atual', 'pendente', 'pendente', 'pendente']);
});
