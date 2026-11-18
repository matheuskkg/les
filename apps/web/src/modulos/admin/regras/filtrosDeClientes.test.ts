import assert from 'node:assert/strict';
import test from 'node:test';
import {
  GENEROS,
  consultaDoEstado,
  lerEstadoDaConsulta,
  rotuloDe,
  urlDaConsulta,
  urlDeVoltaAosClientes,
  urlDoCliente,
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

test('rotuloDe', () => {
  assert.equal(rotuloDe(GENEROS, 'NAO_INFORMADO'), 'Não informado');
});

test('urlDoCliente e urlDeVoltaAosClientes preservam todos os filtros (RF-CLI-004)', () => {
  const estado = lerEstadoDaConsulta(
    '?nome=ana&cpf=529.982.247-25&email=ana%40exemplo.com&telefone=11999999999' +
      '&cidade=Sao Paulo&dataNascimentoDe=1980-01-01&dataNascimentoAte=2000-12-31' +
      '&genero=FEMININO&genero=OUTRO&situacao=ATIVA&situacao=INATIVA&uf=SP&uf=RJ' +
      '&ordem=dataNascimento,desc&pagina=3',
  );
  const consulta = urlDaConsulta(estado);
  const busca = consulta.slice(consulta.indexOf('?'));
  const detalhe = urlDoCliente(42, busca);

  assert.equal(detalhe, `/admin/clientes/42${busca}`);
  const parametros = new URLSearchParams(detalhe.slice(detalhe.indexOf('?')));
  assert.equal(parametros.get('nome'), 'ana');
  assert.equal(parametros.get('cpf'), '529.982.247-25');
  assert.equal(parametros.get('email'), 'ana@exemplo.com');
  assert.equal(parametros.get('telefone'), '11999999999');
  assert.equal(parametros.get('cidade'), 'Sao Paulo');
  assert.equal(parametros.get('dataNascimentoDe'), '1980-01-01');
  assert.equal(parametros.get('dataNascimentoAte'), '2000-12-31');
  assert.deepEqual(parametros.getAll('genero'), ['FEMININO', 'OUTRO']);
  assert.deepEqual(parametros.getAll('situacao'), ['ATIVA', 'INATIVA']);
  assert.deepEqual(parametros.getAll('uf'), ['SP', 'RJ']);
  assert.equal(parametros.get('ordem'), 'dataNascimento,desc');
  assert.equal(parametros.get('pagina'), '3');

  const volta = urlDeVoltaAosClientes(detalhe.slice(detalhe.indexOf('?')));
  assert.equal(volta, consulta);
  assert.deepEqual(lerEstadoDaConsulta(volta.slice(volta.indexOf('?'))), estado);
});

test('urlDoCliente e urlDeVoltaAosClientes ignoram inválidos e padrões (RF-CLI-004)', () => {
  assert.equal(urlDoCliente(7, '?genero=XYZ&ordem=nome,asc&pagina=1'), '/admin/clientes/7');
  assert.equal(
    urlDeVoltaAosClientes('?uf=ZZ&situacao=ATIVA&ordem=nome,asc'),
    '/admin/clientes?situacao=ATIVA',
  );
});
