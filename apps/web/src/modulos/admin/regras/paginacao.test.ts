import assert from 'node:assert/strict';
import test from 'node:test';
import {
  OPCOES_DE_TAMANHO,
  TAMANHO_MAXIMO,
  TAMANHO_PADRAO,
  TAMANHOS_DISPONIVEIS,
  normalizarPagina,
  normalizarTamanho,
} from './paginacao.ts';

test('RNF-GER-001: as opções respeitam o limite de 100 itens por página do server', () => {
  assert.equal(TAMANHO_PADRAO, 10);
  assert.equal(TAMANHO_MAXIMO, 100);
  assert.deepEqual([...TAMANHOS_DISPONIVEIS], [10, 20, 50, 100]);
  assert.deepEqual(OPCOES_DE_TAMANHO, [
    { valor: 10, rotulo: '10' },
    { valor: 20, rotulo: '20' },
    { valor: 50, rotulo: '50' },
    { valor: 100, rotulo: '100' },
  ]);
  for (const valor of TAMANHOS_DISPONIVEIS) {
    assert.ok(valor >= 1 && valor <= TAMANHO_MAXIMO);
  }
});

test('RNF-GER-001: normalizarTamanho aceita só as opções oferecidas', () => {
  for (const valor of TAMANHOS_DISPONIVEIS) {
    assert.equal(normalizarTamanho(valor), valor);
    assert.equal(normalizarTamanho(String(valor)), valor);
    assert.equal(normalizarTamanho(` ${valor} `), valor);
  }
  assert.equal(normalizarTamanho(null), TAMANHO_PADRAO);
  assert.equal(normalizarTamanho(''), TAMANHO_PADRAO);
  assert.equal(normalizarTamanho('0'), TAMANHO_PADRAO);
  assert.equal(normalizarTamanho('-5'), TAMANHO_PADRAO);
  assert.equal(normalizarTamanho('101'), TAMANHO_PADRAO);
  assert.equal(normalizarTamanho('15'), TAMANHO_PADRAO);
  assert.equal(normalizarTamanho('10abc'), TAMANHO_PADRAO);
  assert.equal(normalizarTamanho(Number.NaN), TAMANHO_PADRAO);
});

test('normalizarPagina mantém páginas além do fim para a tela avisar', () => {
  assert.equal(normalizarPagina('3'), 3);
  assert.equal(normalizarPagina(2), 2);
  assert.equal(normalizarPagina(' 4 '), 4);
  assert.equal(normalizarPagina('999'), 999);
  assert.equal(normalizarPagina('0'), 1);
  assert.equal(normalizarPagina('-2'), 1);
  assert.equal(normalizarPagina('3,5'), 1);
  assert.equal(normalizarPagina(undefined), 1);
});
