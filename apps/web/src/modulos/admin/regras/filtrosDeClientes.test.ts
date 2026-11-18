import assert from 'node:assert/strict';
import test from 'node:test';
import {
  GENEROS,
  consultaDoEstado,
  lerEstadoDaConsulta,
  rotuloDe,
  urlDaConsulta,
} from './filtrosDeClientes.ts';
import { TAMANHO_PADRAO } from './paginacao.ts';

test('lerEstadoDaConsulta usa os padrões e ignora valores desconhecidos', () => {
  const estado = lerEstadoDaConsulta('?genero=FEMININO&genero=XYZ&situacao=INATIVA&uf=SP&uf=ZZ&ordem=cpf,asc&pagina=0');
  assert.deepEqual(estado.filtros.genero, ['FEMININO']);
  assert.deepEqual(estado.filtros.situacao, ['INATIVA']);
  assert.deepEqual(estado.filtros.uf, ['SP']);
  assert.equal(estado.ordem, 'nome,asc');
  assert.equal(estado.pagina, 1);
  assert.equal(estado.tamanho, TAMANHO_PADRAO);
});

test('urlDaConsulta e lerEstadoDaConsulta fazem ida e volta', () => {
  const estado = lerEstadoDaConsulta(
    '?nome=ana&cpf=529.982.247-25&genero=FEMININO&genero=MASCULINO&ordem=dataNascimento,desc&tamanho=50&pagina=3',
  );
  const url = urlDaConsulta(estado);
  assert.ok(url.startsWith('/admin/clientes?'));
  assert.ok(url.includes('tamanho=50'));
  assert.deepEqual(lerEstadoDaConsulta(url.slice(url.indexOf('?'))), estado);
  assert.equal(urlDaConsulta(lerEstadoDaConsulta('')), '/admin/clientes');
});

test('RNF-GER-001: tamanho inválido na URL volta ao padrão e não é escrito na url', () => {
  assert.equal(lerEstadoDaConsulta('?tamanho=101').tamanho, TAMANHO_PADRAO);
  assert.equal(lerEstadoDaConsulta('?tamanho=15').tamanho, TAMANHO_PADRAO);
  assert.ok(!urlDaConsulta(lerEstadoDaConsulta('?tamanho=15')).includes('tamanho'));
});

test('consultaDoEstado omite vazios, usa o tamanho escolhido e converte a página para a API', () => {
  const consulta = consultaDoEstado(
    lerEstadoDaConsulta('?nome=%20ana%20&situacao=ATIVA&tamanho=20&pagina=2'),
  );
  assert.deepEqual(consulta, {
    pagina: 1,
    tamanho: 20,
    ordem: ['nome,asc'],
    nome: 'ana',
    situacao: ['ATIVA'],
  });
  assert.equal(consultaDoEstado(lerEstadoDaConsulta('')).tamanho, TAMANHO_PADRAO);
});

test('rotuloDe', () => {
  assert.equal(rotuloDe(GENEROS, 'NAO_INFORMADO'), 'Não informado');
});
