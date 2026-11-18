import assert from 'node:assert/strict';
import test from 'node:test';
import { consultaDoEstado, lerEstadoDaConsulta, numeroValido, urlDaConsulta } from './filtrosDePedidos.ts';
import { TAMANHO_PADRAO } from './paginacao.ts';

test('lerEstadoDaConsulta usa os padrões e ignora valores desconhecidos', () => {
  const estado = lerEstadoDaConsulta(
    '?status=ENTREGUE&status=CANCELADO&status=APROVADA&situacao=TROCADO&situacao=EM_TROCA&situacao=X'
      + '&ordem=cliente,asc&pagina=0',
  );
  assert.deepEqual(estado.filtros.status, ['APROVADA', 'ENTREGUE']);
  assert.deepEqual(estado.filtros.situacao, ['EM_TROCA', 'TROCADO']);
  assert.equal(estado.ordem, 'realizadoEm,desc');
  assert.equal(estado.pagina, 1);
  assert.equal(estado.tamanho, TAMANHO_PADRAO);
});

test('urlDaConsulta e lerEstadoDaConsulta fazem ida e volta, com filtros e tamanho', () => {
  const estado = lerEstadoDaConsulta(
    '?numero=10001&clienteId=1&cliente=ana&cpf=529.982.247-25&realizadoEmDe=2026-10-01&realizadoEmAte=2026-10-24'
      + '&status=APROVADA&status=EM_TRANSPORTE&situacao=TROCA_RECUSADA&ordem=total,asc&tamanho=100&pagina=2',
  );
  const url = urlDaConsulta(estado);
  assert.ok(url.startsWith('/admin/pedidos?'));
  assert.ok(url.includes('tamanho=100'));
  assert.deepEqual(lerEstadoDaConsulta(url.slice(url.indexOf('?'))), estado);
  assert.equal(urlDaConsulta(lerEstadoDaConsulta('')), '/admin/pedidos');
});

test('consultaDoEstado omite vazios, usa o tamanho escolhido e converte número e página para a API', () => {
  const consulta = consultaDoEstado(
    lerEstadoDaConsulta(
      '?numero=%2010001%20&cliente=%20&status=APROVADA&situacao=EM_TROCA&realizadoEmAte=2026-10-24&tamanho=50&pagina=3',
    ),
  );
  assert.deepEqual(consulta, {
    pagina: 2,
    tamanho: 50,
    ordem: ['realizadoEm,desc'],
    numero: 10001,
    realizadoEmAte: '2026-10-24',
    status: ['APROVADA'],
    situacao: ['EM_TROCA'],
  });
  assert.equal(consultaDoEstado(lerEstadoDaConsulta('')).tamanho, TAMANHO_PADRAO);
});

test('consultaDoEstado não envia número que não é inteiro', () => {
  assert.equal(numeroValido(''), true);
  assert.equal(numeroValido(' 10001 '), true);
  assert.equal(numeroValido('12a'), false);
  assert.equal(consultaDoEstado(lerEstadoDaConsulta('?numero=12a')).numero, undefined);
  assert.equal(consultaDoEstado(lerEstadoDaConsulta('?numero=-3')).numero, undefined);
});
