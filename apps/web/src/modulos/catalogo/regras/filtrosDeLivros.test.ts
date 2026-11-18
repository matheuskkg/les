import assert from 'node:assert/strict';
import test from 'node:test';
import { buscaDaUrl } from '../../../compartilhado/url.ts';
import {
  ORDEM_PADRAO,
  TAMANHO_DA_PAGINA,
  buscaDoEstado,
  consultaDoEstado,
  filtrosEmUso,
  filtrosVazios,
  lerEstadoDaVitrine,
  ordemValida,
  urlDeVoltaAVitrine,
  urlDoEstado,
  urlDoLivro,
  type EstadoDaVitrine,
} from './filtrosDeLivros.ts';

const estadoVazio: EstadoDaVitrine = {
  filtros: filtrosVazios(),
  ordem: ORDEM_PADRAO,
  pagina: 1,
};

test('lerEstadoDaVitrine usa os padrões com a busca vazia', () => {
  assert.deepEqual(lerEstadoDaVitrine(''), estadoVazio);
  assert.deepEqual(lerEstadoDaVitrine('?'), estadoVazio);
});

test('lerEstadoDaVitrine lê filtros, categorias repetidas e paginação', () => {
  const estado = lerEstadoDaVitrine(
    '?titulo=dom&autor=+machado+&categoria=2&categoria=7&categoria=2&ordem=ano,desc&pagina=3',
  );

  assert.deepEqual(estado, {
    filtros: {
      q: '',
      titulo: 'dom',
      autor: 'machado',
      editora: '',
      isbn: '',
      codigo: '',
      anoDe: '',
      anoAte: '',
      categorias: ['2', '7'],
    },
    ordem: 'ano,desc',
    pagina: 3,
  });
});

test('lerEstadoDaVitrine ignora ordem, página e categorias inválidas', () => {
  const estado = lerEstadoDaVitrine('?ordem=preco,asc&pagina=0&pagina=abc&categoria=livro');

  assert.equal(estado.ordem, ORDEM_PADRAO);
  assert.equal(estado.pagina, 1);
  assert.deepEqual(estado.filtros.categorias, []);
  assert.equal(ordemValida('editora,asc'), 'editora,asc');
  assert.equal(ordemValida('editora,asc;drop table'), ORDEM_PADRAO);
  assert.equal(ordemValida(null), ORDEM_PADRAO);
});

test('buscaDoEstado omite os padrões e mantém os valores codificados', () => {
  assert.equal(buscaDoEstado(estadoVazio), '');

  const busca = buscaDoEstado({
    filtros: {
      ...filtrosVazios(),
      titulo: 'cem anos',
      autor: 'machado',
      categorias: ['3', '9'],
    },
    ordem: 'titulo,desc',
    pagina: 2,
  });

  assert.equal(
    busca,
    '?titulo=cem+anos&autor=machado&categoria=3&categoria=9&ordem=titulo%2Cdesc&pagina=2',
  );
});

test('estado e busca fazem round-trip', () => {
  const estado: EstadoDaVitrine = {
    filtros: {
      q: '',
      titulo: 'a culpa',
      autor: 'saramago',
      editora: 'companhia',
      isbn: '9788535914849',
      codigo: '42',
      anoDe: '1990',
      anoAte: '2010',
      categorias: ['1', '5'],
    },
    ordem: 'editora,asc',
    pagina: 4,
  };

  const lido = lerEstadoDaVitrine(buscaDoEstado(estado));
  assert.deepEqual(lido, estado);
  assert.equal(urlDoEstado(estado), `/${buscaDoEstado(estado)}`);
  assert.equal(urlDoEstado(estadoVazio), '/');
});

test('consultaDoEstado converte o estado nos parâmetros do contrato', () => {
  const consulta = consultaDoEstado({
    filtros: {
      q: '',
      titulo: ' dom ',
      autor: 'machado',
      editora: '',
      isbn: '',
      codigo: '7',
      anoDe: '1900',
      anoAte: '1930',
      categorias: ['2', '8'],
    },
    ordem: 'ano,desc',
    pagina: 2,
  });

  assert.deepEqual(consulta, {
    titulo: 'dom',
    autor: 'machado',
    codigo: 7,
    anoDe: 1900,
    anoAte: 1930,
    categoria: [2, 8],
    ordem: ['ano,desc'],
    pagina: 1,
    tamanho: TAMANHO_DA_PAGINA,
  });

  assert.deepEqual(consultaDoEstado(estadoVazio), {
    ordem: [ORDEM_PADRAO],
    pagina: 0,
    tamanho: TAMANHO_DA_PAGINA,
  });
});

test('consultaDoEstado descarta números inválidos e preserva intervalos invertidos', () => {
  const consulta = consultaDoEstado({
    ...estadoVazio,
    filtros: {
      ...filtrosVazios(),
      codigo: 'abc',
      anoDe: '2020',
      anoAte: '1990',
      categorias: ['3', 'x'],
    },
  });

  assert.equal(consulta.codigo, undefined);
  assert.equal(consulta.anoDe, 2020);
  assert.equal(consulta.anoAte, 1990);
  assert.deepEqual(consulta.categoria, [3]);
});

test('RF-LIV-005: a busca livre q vai da URL para a consulta', () => {
  const estado = lerEstadoDaVitrine('?q=+machado+');

  assert.equal(estado.filtros.q, 'machado');
  assert.equal(consultaDoEstado(estado).q, 'machado');
  assert.equal(urlDoEstado(estado), '/?q=machado');
});

test('RF-LIV-005: urlDoLivro e urlDeVoltaAVitrine preservam a busca no retorno', () => {
  const busca = '?q=machado&categoria=3&ordem=ano,desc&pagina=2';
  const detalhe = urlDoLivro(12, busca);
  const volta = urlDeVoltaAVitrine(buscaDaUrl(detalhe));

  assert.equal(volta, urlDoEstado(lerEstadoDaVitrine(busca)));
  assert.deepEqual(lerEstadoDaVitrine(volta.slice(volta.indexOf('?'))), lerEstadoDaVitrine(busca));
});

test('RF-LIV-005: sem filtros o link do livro fica limpo e o voltar é a vitrine', () => {
  assert.equal(urlDoLivro(12, ''), '/livros/12');
  assert.equal(urlDoLivro(12, '?ordem=titulo,asc&pagina=1&categoria=x'), '/livros/12');
  assert.equal(urlDeVoltaAVitrine(''), '/');
  assert.equal(urlDeVoltaAVitrine('?de=https%3A%2F%2Fexemplo.com'), '/');
});

test('filtrosEmUso conta textos preenchidos e categorias', () => {
  assert.equal(filtrosEmUso(filtrosVazios()), 0);
  assert.equal(filtrosEmUso({ ...filtrosVazios(), q: 'dom', anoDe: '1990', categorias: ['1', '2'] }), 4);
});
