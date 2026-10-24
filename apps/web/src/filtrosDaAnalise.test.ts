import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  consultaDaAnalise,
  corDaCategoria,
  lerEstadoDaAnalise,
  rotuloDoMes,
  somarMeses,
  urlDaAnalise,
  urlDaExportacao,
} from './filtrosDaAnalise.ts';

const HOJE = new Date(2026, 9, 24, 12, 0);

test('RN-ANA-002: sem filtros, o período são os 12 últimos meses contando o atual', () => {
  assert.deepEqual(lerEstadoDaAnalise('', HOJE), { inicio: '2025-11', fim: '2026-10', categorias: [] });
});

test('RF-ANA-002: lê os meses e as categorias da URL, ignorando valores malformados e repetidos', () => {
  assert.deepEqual(lerEstadoDaAnalise('?inicio=2025-03&fim=2025-06&categoria=2&categoria=x&categoria=2&categoria=5', HOJE), {
    inicio: '2025-03',
    fim: '2025-06',
    categorias: [2, 5],
  });
  assert.deepEqual(lerEstadoDaAnalise('?inicio=2025-13&fim=2025-06', HOJE).inicio, '2024-07');
});

test('RF-ANA-002: o fim anterior ao início fica na URL para o server responder com a regra', () => {
  assert.deepEqual(lerEstadoDaAnalise('?inicio=2025-06&fim=2025-03', HOJE), {
    inicio: '2025-06',
    fim: '2025-03',
    categorias: [],
  });
});

test('soma meses atravessando o ano', () => {
  assert.equal(somarMeses('2026-10', -11), '2025-11');
  assert.equal(somarMeses('2025-12', 1), '2026-01');
  assert.equal(somarMeses('2025-01', -1), '2024-12');
});

test('RF-ANA-003: sem categoria escolhida, a consulta leva todas do catálogo', () => {
  const estado = { inicio: '2025-03', fim: '2025-03', categorias: [] };
  assert.deepEqual(consultaDaAnalise(estado, [1, 2, 3]), { inicio: '2025-03', fim: '2025-03', categoria: [1, 2, 3] });
  assert.deepEqual(consultaDaAnalise({ ...estado, categorias: [3] }, [1, 2, 3]).categoria, [3]);
});

test('RF-ANA-004: a exportação usa os mesmos filtros da tela', () => {
  const estado = { inicio: '2025-03', fim: '2025-05', categorias: [2, 4] };
  assert.equal(urlDaAnalise(estado), '/admin/analise?inicio=2025-03&fim=2025-05&categoria=2&categoria=4');
  assert.equal(
    urlDaExportacao(consultaDaAnalise(estado, [])),
    '/api/admin/analise/exportacao?inicio=2025-03&fim=2025-05&categoria=2&categoria=4',
  );
});

test('RNF-ANA-001: rótulo mm/aaaa e cor fixa pela posição da categoria no catálogo', () => {
  assert.equal(rotuloDoMes('2025-03'), '03/2025');
  assert.equal(corDaCategoria(3, [1, 2, 3]), '#1baf7a');
  assert.equal(corDaCategoria(3, [3, 1, 2]), '#1baf7a');
  assert.equal(corDaCategoria(3, [1, 2, 3]), corDaCategoria(3, [1, 2, 3, 4]));
});
