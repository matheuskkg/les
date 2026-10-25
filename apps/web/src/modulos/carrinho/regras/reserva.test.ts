import assert from 'node:assert/strict';
import test from 'node:test';
import { tempoRestante } from './reserva.ts';

const prazo = '2026-10-24T18:00:00Z';
const antes = (ms: number) => Date.parse(prazo) - ms;

test('tempoRestante mostra minutos e segundos (RNF-VEN-001)', () => {
  assert.deepEqual(tempoRestante(prazo, antes(29 * 60_000 + 5_000)), {
    restante: 29 * 60_000 + 5_000,
    texto: '29:05',
    acabando: false,
  });
});

test('tempoRestante destaca os últimos cinco minutos e para no zero', () => {
  assert.equal(tempoRestante(prazo, antes(4 * 60_000)).acabando, true);
  assert.deepEqual(tempoRestante(prazo, antes(-1_000)), { restante: 0, texto: '0:00', acabando: true });
});

test('tempoRestante inclui as horas quando passa de uma', () => {
  assert.equal(tempoRestante(prazo, antes(3_600_000 + 61_000)).texto, '1:01:01');
});
