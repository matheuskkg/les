import assert from 'node:assert/strict';
import test from 'node:test';
import {
  GENEROS,
  consultaDoEstado,
  formatarCpf,
  lerEstadoDaConsulta,
  rotuloDe,
  urlDaConsulta,
} from './filtrosDeClientes.ts';

test('lerEstadoDaConsulta usa os padrões e ignora valores desconhecidos', () => {
  const estado = lerEstadoDaConsulta('?genero=FEMININO&genero=XYZ&situacao=INATIVA&uf=SP&uf=ZZ&ordem=cpf,asc&pagina=0');
  assert.deepEqual(estado.filtros.genero, ['FEMININO']);
  assert.deepEqual(estado.filtros.situacao, ['INATIVA']);
  assert.deepEqual(estado.filtros.uf, ['SP']);
  assert.equal(estado.ordem, 'nome,asc');
  assert.equal(estado.pagina, 1);
});

test('urlDaConsulta e lerEstadoDaConsulta fazem ida e volta', () => {
  const estado = lerEstadoDaConsulta(
    '?nome=ana&cpf=529.982.247-25&genero=FEMININO&genero=MASCULINO&ordem=dataNascimento,desc&pagina=3',
  );
  const url = urlDaConsulta(estado);
  assert.ok(url.startsWith('/admin/clientes?'));
  assert.deepEqual(lerEstadoDaConsulta(url.slice(url.indexOf('?'))), estado);
  assert.equal(urlDaConsulta(lerEstadoDaConsulta('')), '/admin/clientes');
});

test('consultaDoEstado omite vazios e converte a página para a API', () => {
  const consulta = consultaDoEstado(lerEstadoDaConsulta('?nome=%20ana%20&situacao=ATIVA&pagina=2'));
  assert.deepEqual(consulta, {
    pagina: 1,
    tamanho: 20,
    ordem: ['nome,asc'],
    nome: 'ana',
    situacao: ['ATIVA'],
  });
});

test('formatarCpf e rotuloDe', () => {
  assert.equal(formatarCpf('52998224725'), '529.982.247-25');
  assert.equal(formatarCpf('123'), '123');
  assert.equal(rotuloDe(GENEROS, 'NAO_INFORMADO'), 'Não informado');
});
