import assert from 'node:assert/strict';
import test from 'node:test';
import { alternarStatus, consultaDasTrocas, lerEstadoDasTrocas, urlDasTrocas } from './filtrosDeTrocas.ts';
import { TAMANHO_PADRAO } from './paginacao.ts';

test('RF-VEN-013: sem status na URL, a tela mostra todas as trocas com o tamanho padrão', () => {
  assert.deepEqual(lerEstadoDasTrocas(''), { status: [], pagina: 1, tamanho: TAMANHO_PADRAO });
  assert.equal(urlDasTrocas({ status: [], pagina: 1, tamanho: TAMANHO_PADRAO }), '/admin/trocas');
  assert.deepEqual(consultaDasTrocas({ status: [], pagina: 1, tamanho: TAMANHO_PADRAO }), {
    pagina: 0,
    tamanho: TAMANHO_PADRAO,
    ordem: ['solicitadaEm,desc'],
  });
});

test('RF-VEN-013: vários status, página e tamanho fazem ida e volta pela URL', () => {
  const estado = lerEstadoDasTrocas('?status=EM_TROCA&status=TROCADO&tamanho=20&pagina=3');
  assert.deepEqual(estado, { status: ['EM_TROCA', 'TROCADO'], pagina: 3, tamanho: 20 });
  const url = urlDasTrocas(estado);
  assert.equal(url, '/admin/trocas?status=EM_TROCA&status=TROCADO&tamanho=20&pagina=3');
  assert.deepEqual(lerEstadoDasTrocas(url.slice(url.indexOf('?'))), estado);
});

test('RF-VEN-013: status inválidos e repetidos são ignorados, na ordem do ciclo', () => {
  assert.deepEqual(
    lerEstadoDasTrocas('?status=TROCADO&status=CANCELADA&status=EM_TROCA&status=TROCADO&tamanho=7&pagina=0'),
    { status: ['EM_TROCA', 'TROCADO'], pagina: 1, tamanho: TAMANHO_PADRAO },
  );
});

test('RF-VEN-013: alternar status mantém a ordem da lista de opções', () => {
  assert.deepEqual(alternarStatus([], 'TROCADO'), ['TROCADO']);
  assert.deepEqual(alternarStatus(['TROCADO'], 'EM_TROCA'), ['EM_TROCA', 'TROCADO']);
  assert.deepEqual(alternarStatus(['EM_TROCA', 'TROCADO'], 'EM_TROCA'), ['TROCADO']);
});

test('RF-VEN-013: a consulta envia todos os status marcados e preserva página e tamanho', () => {
  assert.deepEqual(consultaDasTrocas({ status: ['EM_TROCA', 'TROCA_RECUSADA'], pagina: 2, tamanho: 10 }), {
    status: ['EM_TROCA', 'TROCA_RECUSADA'],
    pagina: 1,
    tamanho: 10,
    ordem: ['solicitadaEm,desc'],
  });
  assert.deepEqual(consultaDasTrocas({ status: [], pagina: 1, tamanho: 100 }), {
    pagina: 0,
    tamanho: 100,
    ordem: ['solicitadaEm,desc'],
  });
});
