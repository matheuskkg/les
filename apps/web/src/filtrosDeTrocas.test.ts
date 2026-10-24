import assert from 'node:assert/strict';
import test from 'node:test';
import { consultaDasTrocas, lerEstadoDasTrocas, urlDasTrocas } from './filtrosDeTrocas.ts';

test('RF-VEN-013: sem filtro na URL, a tela mostra as trocas EM TROCA', () => {
  assert.deepEqual(lerEstadoDasTrocas(''), { status: 'EM_TROCA', pagina: 1 });
  assert.deepEqual(lerEstadoDasTrocas('?status=CANCELADA&pagina=0'), { status: 'EM_TROCA', pagina: 1 });
  assert.equal(urlDasTrocas({ status: 'EM_TROCA', pagina: 1 }), '/admin/trocas');
});

test('RF-VEN-013: status e página fazem ida e volta pela URL', () => {
  const estado = lerEstadoDasTrocas('?status=TROCA_RECUSADA&pagina=3');
  assert.deepEqual(estado, { status: 'TROCA_RECUSADA', pagina: 3 });
  const url = urlDasTrocas(estado);
  assert.deepEqual(lerEstadoDasTrocas(url.slice(url.indexOf('?'))), estado);
});

test('RF-VEN-013: "todas" não envia status e a página da API começa em 0', () => {
  assert.deepEqual(consultaDasTrocas({ status: 'TODAS', pagina: 2 }), {
    pagina: 1,
    tamanho: 20,
    ordem: ['solicitadaEm,desc'],
  });
  assert.deepEqual(consultaDasTrocas({ status: 'EM_TROCA', pagina: 1 }).status, ['EM_TROCA']);
});
