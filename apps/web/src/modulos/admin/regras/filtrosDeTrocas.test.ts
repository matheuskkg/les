import assert from 'node:assert/strict';
import test from 'node:test';
import { consultaDasTrocas, lerEstadoDasTrocas, urlDasTrocas } from './filtrosDeTrocas.ts';
import { TAMANHO_PADRAO } from './paginacao.ts';

test('RF-VEN-013: sem filtro na URL, a tela mostra as trocas EM TROCA', () => {
  assert.deepEqual(lerEstadoDasTrocas(''), { status: 'EM_TROCA', pagina: 1, tamanho: TAMANHO_PADRAO });
  assert.deepEqual(lerEstadoDasTrocas('?status=CANCELADA&pagina=0'), {
    status: 'EM_TROCA',
    pagina: 1,
    tamanho: TAMANHO_PADRAO,
  });
  assert.equal(urlDasTrocas({ status: 'EM_TROCA', pagina: 1, tamanho: TAMANHO_PADRAO }), '/admin/trocas');
});

test('RF-VEN-013: status, página e tamanho fazem ida e volta pela URL', () => {
  const estado = lerEstadoDasTrocas('?status=TROCA_RECUSADA&tamanho=20&pagina=3');
  assert.deepEqual(estado, { status: 'TROCA_RECUSADA', pagina: 3, tamanho: 20 });
  const url = urlDasTrocas(estado);
  assert.ok(url.includes('tamanho=20'));
  assert.deepEqual(lerEstadoDasTrocas(url.slice(url.indexOf('?'))), estado);
});

test('RNF-GER-001: "todas" não envia status e a página da API começa em 0', () => {
  assert.deepEqual(consultaDasTrocas({ status: 'TODAS', pagina: 2, tamanho: 10 }), {
    pagina: 1,
    tamanho: 10,
    ordem: ['solicitadaEm,desc'],
  });
  assert.deepEqual(consultaDasTrocas({ status: 'EM_TROCA', pagina: 1, tamanho: 10 }).status, ['EM_TROCA']);
});
