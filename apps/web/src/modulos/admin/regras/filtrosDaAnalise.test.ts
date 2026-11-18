import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  consultaDaAnalise,
  corDaCategoria,
  lerEstadoDaAnalise,
  rotuloDoDia,
  rotuloDoMes,
  somarMeses,
  urlDaAnalise,
  urlDaExportacao,
} from './filtrosDaAnalise.ts';

const HOJE = new Date(2026, 9, 24, 12, 0);

test('RN-ANA-002: sem filtros, o período começa no primeiro dia do 12º mês anterior e termina hoje', () => {
  assert.deepEqual(lerEstadoDaAnalise('', HOJE), {
    inicio: '2025-11-01',
    fim: '2026-10-24',
    categorias: [],
  });
});

test('RF-ANA-002: lê as datas e as categorias da URL, ignorando valores malformados e repetidos', () => {
  assert.deepEqual(
    lerEstadoDaAnalise('?inicio=2025-03-15&fim=2025-06-20&categoria=2&categoria=x&categoria=2&categoria=5', HOJE),
    { inicio: '2025-03-15', fim: '2025-06-20', categorias: [2, 5] },
  );
  // Um dia que não existe no mês cai no padrão, que começa no primeiro dia do mês 12 meses antes do fim da URL.
  assert.equal(lerEstadoDaAnalise('?inicio=2025-02-30&fim=2025-06-20', HOJE).inicio, '2024-07-01');
});

test('RF-ANA-002: datas fora do formatoAAAA-MM-DD também caem no período padrão', () => {
  assert.equal(lerEstadoDaAnalise('?inicio=2025-13-01', HOJE).inicio, '2025-11-01');
  assert.equal(lerEstadoDaAnalise('?inicio=2025-03', HOJE).inicio, '2025-11-01');
  assert.equal(lerEstadoDaAnalise('?fim=2025-06-31', HOJE).fim, '2026-10-24');
});

test('RF-ANA-002: a data final anterior à inicial fica na URL para o server responder com a regra', () => {
  assert.deepEqual(lerEstadoDaAnalise('?inicio=2025-12-01&fim=2025-01-31', HOJE), {
    inicio: '2025-12-01',
    fim: '2025-01-31',
    categorias: [],
  });
});

test('soma meses atravessando o ano', () => {
  assert.equal(somarMeses('2026-10', -11), '2025-11');
  assert.equal(somarMeses('2025-12', 1), '2026-01');
  assert.equal(somarMeses('2025-01', -1), '2024-12');
});

test('RF-ANA-003: sem categoria escolhida, a consulta leva todas do catálogo', () => {
  const estado = { inicio: '2025-03-01', fim: '2025-03-31', categorias: [] };
  assert.deepEqual(consultaDaAnalise(estado, [1, 2, 3]), {
    inicio: '2025-03-01',
    fim: '2025-03-31',
    categoria: [1, 2, 3],
  });
  assert.deepEqual(consultaDaAnalise({ ...estado, categorias: [3] }, [1, 2, 3]).categoria, [3]);
});

test('RF-ANA-004: a exportação usa os mesmos filtros da tela', () => {
  const estado = { inicio: '2025-03-01', fim: '2025-05-31', categorias: [2, 4] };
  assert.equal(urlDaAnalise(estado), '/admin/analise?inicio=2025-03-01&fim=2025-05-31&categoria=2&categoria=4');
  assert.equal(
    urlDaExportacao(consultaDaAnalise(estado, [])),
    '/api/admin/analise/exportacao?inicio=2025-03-01&fim=2025-05-31&categoria=2&categoria=4',
  );
});

test('DEC-19: cabeçalho mostra a data exata e o eixo mantém o mês', () => {
  assert.equal(rotuloDoDia('2025-03-15'), '15/03/2025');
  assert.equal(rotuloDoMes('2025-03'), '03/2025');
});

test('RNF-ANA-001: cor fixa pela posição da categoria no catálogo', () => {
  assert.equal(corDaCategoria(3, [1, 2, 3]), '#1baf7a');
  assert.equal(corDaCategoria(3, [3, 1, 2]), '#1baf7a');
  assert.equal(corDaCategoria(3, [1, 2, 3]), corDaCategoria(3, [1, 2, 3, 4]));
});
