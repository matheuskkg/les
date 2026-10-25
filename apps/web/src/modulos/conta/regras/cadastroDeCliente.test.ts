import assert from 'node:assert/strict';
import test from 'node:test';
import {
  cartaoVazio,
  enderecoVazio,
  formatarCep,
  mascararCpf,
  formatarValidade,
  formularioVazio,
  montarCadastro,
  somenteDigitos,
  validadeParaApi,
  validarCadastro,
  type FormularioDeCadastro,
} from './cadastroDeCliente.ts';

function formularioPreenchido(): FormularioDeCadastro {
  return {
    ...formularioVazio(),
    nome: '  Ana da Silva  ',
    genero: 'FEMININO',
    dataNascimento: '1990-05-12',
    cpf: '529.982.247-25',
    email: '  ana@exemplo.com ',
    telefone: { tipo: 'CELULAR', ddd: '(11)', numero: '98765-4321' },
    senha: 'Senha@123',
    confirmacaoSenha: 'Senha@123',
    enderecos: [
      {
        ...enderecoVazio(),
        nomeCurto: 'Casa',
        tipoEndereco: 'CASA',
        tipoLogradouro: 'RUA',
        logradouro: 'Rua das Flores',
        numero: '123',
        bairro: 'Centro',
        cep: '01001-000',
        cidade: 'São Paulo',
        uf: 'SP',
        observacoes: '  Apto 42  ',
      },
    ],
    cartoes: [
      {
        ...cartaoVazio(),
        numero: '4111 1111 1111 1111',
        nomeImpresso: '  ANA DA SILVA ',
        bandeiraId: '1',
        validade: '2027-08',
      },
    ],
  };
}

test('somenteDigitos remove máscaras e separadores', () => {
  assert.equal(somenteDigitos('529.982.247-25'), '52998224725');
  assert.equal(somenteDigitos('(11) 98765-4321'), '11987654321');
  assert.equal(somenteDigitos('01001-000'), '01001000');
  assert.equal(somenteDigitos(''), '');
  assert.equal(somenteDigitos('abc'), '');
});

test('mascararCpf monta a máscara conforme os dígitos chegam', () => {
  assert.equal(mascararCpf(''), '');
  assert.equal(mascararCpf('529'), '529');
  assert.equal(mascararCpf('5299'), '529.9');
  assert.equal(mascararCpf('529982'), '529.982');
  assert.equal(mascararCpf('529982247'), '529.982.247');
  assert.equal(mascararCpf('52998224725'), '529.982.247-25');
  assert.equal(mascararCpf('529.982.247-25'), '529.982.247-25');
  assert.equal(mascararCpf('529982247259999'), '529.982.247-25');
});

test('formatarCep monta a máscara de 8 dígitos', () => {
  assert.equal(formatarCep(''), '');
  assert.equal(formatarCep('01001'), '01001');
  assert.equal(formatarCep('01001000'), '01001-000');
  assert.equal(formatarCep('01001-000'), '01001-000');
  assert.equal(formatarCep('0100100099'), '01001-000');
});

test('validadeParaApi normaliza mês e ano (DEC-07)', () => {
  assert.equal(validadeParaApi('2027-08'), '2027-08');
  assert.equal(validadeParaApi('2027-8'), '2027-08');
  assert.equal(validadeParaApi('08/2027'), '2027-08');
  assert.equal(validadeParaApi('8/2027'), '2027-08');
  assert.equal(validadeParaApi('08/27'), '2027-08');
  assert.equal(validadeParaApi('13/2027'), '');
  assert.equal(validadeParaApi('2027-13'), '');
  assert.equal(validadeParaApi('agosto'), '');
  assert.equal(validadeParaApi(''), '');
});

test('formatarValidade exibe mês e ano no padrão MM/AAAA', () => {
  assert.equal(formatarValidade('2027-08'), '08/2027');
  assert.equal(formatarValidade('08/2027'), '08/2027');
  assert.equal(formatarValidade('08/27'), '08/2027');
  assert.equal(formatarValidade('inválida'), '');
});

test('montarCadastro converte o formulário no corpo da API', () => {
  const corpo = montarCadastro(formularioPreenchido());

  assert.deepEqual(corpo, {
    nome: 'Ana da Silva',
    genero: 'FEMININO',
    dataNascimento: '1990-05-12',
    cpf: '52998224725',
    email: 'ana@exemplo.com',
    telefone: { tipo: 'CELULAR', ddd: '11', numero: '987654321' },
    senha: 'Senha@123',
    confirmacaoSenha: 'Senha@123',
    enderecos: [
      {
        nomeCurto: 'Casa',
        tipoEndereco: 'CASA',
        tipoLogradouro: 'RUA',
        logradouro: 'Rua das Flores',
        numero: '123',
        bairro: 'Centro',
        cep: '01001000',
        cidade: 'São Paulo',
        uf: 'SP',
        observacoes: 'Apto 42',
      },
    ],
    cartoes: [
      {
        numero: '4111111111111111',
        nomeImpresso: 'ANA DA SILVA',
        bandeiraId: 1,
        validade: '2027-08',
      },
    ],
  });
});

test('montarCadastro envia listas vazias quando não há cartões', () => {
  const formulario = formularioPreenchido();
  formulario.cartoes = [];

  assert.deepEqual(montarCadastro(formulario).cartoes, []);
});

test('validarCadastro aceita o formulário completo', () => {
  assert.deepEqual(validarCadastro(formularioPreenchido()), {});
});

test('validarCadastro exige os dados pessoais', () => {
  const erros = validarCadastro(formularioVazio());

  assert.equal(erros.nome, 'Informe o nome.');
  assert.equal(erros.genero, 'Informe o gênero.');
  assert.equal(erros.dataNascimento, 'Informe a data de nascimento.');
  assert.equal(erros.cpf, 'Informe o CPF.');
  assert.equal(erros.email, 'Informe o e-mail.');
  assert.equal(erros.ddd, 'Informe o DDD.');
  assert.equal(erros['telefone-numero'], 'Informe o número do telefone.');
  assert.equal(erros.senha, 'Informe a senha.');
  assert.equal(erros.confirmacaoSenha, 'Confirme a senha.');
});

test('validarCadastro confere CPF com 11 dígitos e CEP com 8 dígitos', () => {
  const formulario = formularioPreenchido();
  formulario.cpf = '529.982.247';
  const primeiroEndereco = formulario.enderecos[0];
  assert.ok(primeiroEndereco);
  primeiroEndereco.cep = '0100-100';

  const erros = validarCadastro(formulario);

  assert.equal(erros.cpf, 'Informe um CPF com 11 dígitos.');
  assert.equal(erros['enderecos.0.cep'], 'Informe um CEP com 8 dígitos.');
});

test('validarCadastro exige a confirmação igual à senha (RNF-CLI-002)', () => {
  const formulario = formularioPreenchido();
  formulario.confirmacaoSenha = 'Senha@124';

  assert.equal(validarCadastro(formulario).confirmacaoSenha, 'As senhas não coincidem.');
});

test('validarCadastro exige os campos de cada endereço (RN-CLI-003)', () => {
  const formulario = formularioPreenchido();
  formulario.enderecos = [enderecoVazio()];

  const erros = validarCadastro(formulario);

  assert.equal(erros['enderecos.0.nomeCurto'], 'Informe um nome curto para o endereço.');
  assert.equal(erros['enderecos.0.logradouro'], 'Informe o logradouro.');
  assert.equal(erros['enderecos.0.cep'], 'Informe o CEP.');
  assert.equal(erros['enderecos.0.uf'], 'Informe o estado (UF).');
});

test('validarCadastro exige os campos de cada cartão (RN-CLI-004)', () => {
  const formulario = formularioPreenchido();
  formulario.cartoes = [cartaoVazio()];

  const erros = validarCadastro(formulario);

  assert.equal(erros['cartoes.0.numero'], 'Informe o número do cartão.');
  assert.equal(erros['cartoes.0.nomeImpresso'], 'Informe o nome impresso no cartão.');
  assert.equal(erros['cartoes.0.bandeiraId'], 'Informe a bandeira do cartão.');
  assert.equal(erros['cartoes.0.validade'], 'Informe a validade do cartão.');
});

test('validarCadastro exige ao menos um endereço (RN-CLI-001/RN-CLI-002)', () => {
  const formulario = formularioPreenchido();
  formulario.enderecos = [];

  assert.equal(validarCadastro(formulario).enderecos, 'Informe ao menos um endereço.');
});
