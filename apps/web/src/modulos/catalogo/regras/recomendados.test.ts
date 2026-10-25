import assert from 'node:assert/strict';
import test from 'node:test';
import { aplicarAvaliacao, mostrarRecomendados, type ItemRecomendado } from './recomendados.ts';

function item(id: number): ItemRecomendado {
  return {
    id,
    livro: {
      id: id * 10,
      titulo: `Livro ${id}`,
      autores: [],
      categorias: [],
      editora: 'Editora',
      ano: 2000,
      disponivel: 1,
    },
  };
}

test('RNF-REC-001: avaliação negativa tira o item da lista na hora', () => {
  assert.deepEqual(
    aplicarAvaliacao([item(1), item(2)], 1, 'NEGATIVA').map((atual) => atual.id),
    [2],
  );
});

test('RNF-REC-001: avaliação positiva marca o item e mantém a ordem', () => {
  const itens = aplicarAvaliacao([item(1), item(2)], 2, 'POSITIVA');
  assert.deepEqual(itens.map((atual) => [atual.id, atual.avaliacao]), [
    [1, undefined],
    [2, 'POSITIVA'],
  ]);
});

test('RNF-REC-001: recomendados só na vitrine sem filtros e na primeira página', () => {
  assert.equal(mostrarRecomendados(0, 1), true);
  assert.equal(mostrarRecomendados(1, 1), false);
  assert.equal(mostrarRecomendados(0, 2), false);
});
