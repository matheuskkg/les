import type { components } from '../../../api/esquema';

type CadastroDeCliente = components['schemas']['CadastroDeCliente'];

export type Genero = CadastroDeCliente['genero'];
export type TipoDeTelefone = components['schemas']['TelefoneDoCadastro']['tipo'];
export type TipoDeEndereco = components['schemas']['EnderecoDoCadastro']['tipoEndereco'];
export type TipoDeLogradouro = components['schemas']['EnderecoDoCadastro']['tipoLogradouro'];
export type Uf = components['schemas']['EnderecoDoCadastro']['uf'];
export type EnderecoDaApi = components['schemas']['EnderecoDoCadastro'];
export type CartaoDaApi = components['schemas']['CartaoDoCadastro'];

export interface OpcaoDeCadastro<T extends string = string> {
  valor: T;
  rotulo: string;
}

/** Valores de domínio do cadastro com os rótulos exibidos na UI (DEC-10). */
export const GENEROS: ReadonlyArray<OpcaoDeCadastro<Genero>> = [
  { valor: 'FEMININO', rotulo: 'Feminino' },
  { valor: 'MASCULINO', rotulo: 'Masculino' },
  { valor: 'OUTRO', rotulo: 'Outro' },
  { valor: 'NAO_INFORMADO', rotulo: 'Prefiro não informar' },
];

export const TIPOS_DE_TELEFONE: ReadonlyArray<OpcaoDeCadastro<TipoDeTelefone>> = [
  { valor: 'CELULAR', rotulo: 'Celular' },
  { valor: 'FIXO', rotulo: 'Fixo' },
];

export const TIPOS_DE_ENDERECO: ReadonlyArray<OpcaoDeCadastro<TipoDeEndereco>> = [
  { valor: 'CASA', rotulo: 'Casa' },
  { valor: 'TRABALHO', rotulo: 'Trabalho' },
];

export const TIPOS_DE_LOGRADOURO: ReadonlyArray<OpcaoDeCadastro<TipoDeLogradouro>> = [
  { valor: 'RUA', rotulo: 'Rua' },
  { valor: 'AVENIDA', rotulo: 'Avenida' },
  { valor: 'ALAMEDA', rotulo: 'Alameda' },
  { valor: 'TRAVESSA', rotulo: 'Travessa' },
  { valor: 'PRACA', rotulo: 'Praça' },
  { valor: 'ESTRADA', rotulo: 'Estrada' },
  { valor: 'RODOVIA', rotulo: 'Rodovia' },
  { valor: 'OUTRO', rotulo: 'Outro' },
];

/** As 27 unidades da federação aceitas pelo cadastro (DEC-10). */
export const UFS: ReadonlyArray<OpcaoDeCadastro<Uf>> = [
  { valor: 'AC', rotulo: 'AC — Acre' },
  { valor: 'AL', rotulo: 'AL — Alagoas' },
  { valor: 'AP', rotulo: 'AP — Amapá' },
  { valor: 'AM', rotulo: 'AM — Amazonas' },
  { valor: 'BA', rotulo: 'BA — Bahia' },
  { valor: 'CE', rotulo: 'CE — Ceará' },
  { valor: 'DF', rotulo: 'DF — Distrito Federal' },
  { valor: 'ES', rotulo: 'ES — Espírito Santo' },
  { valor: 'GO', rotulo: 'GO — Goiás' },
  { valor: 'MA', rotulo: 'MA — Maranhão' },
  { valor: 'MT', rotulo: 'MT — Mato Grosso' },
  { valor: 'MS', rotulo: 'MS — Mato Grosso do Sul' },
  { valor: 'MG', rotulo: 'MG — Minas Gerais' },
  { valor: 'PA', rotulo: 'PA — Pará' },
  { valor: 'PB', rotulo: 'PB — Paraíba' },
  { valor: 'PR', rotulo: 'PR — Paraná' },
  { valor: 'PE', rotulo: 'PE — Pernambuco' },
  { valor: 'PI', rotulo: 'PI — Piauí' },
  { valor: 'RJ', rotulo: 'RJ — Rio de Janeiro' },
  { valor: 'RN', rotulo: 'RN — Rio Grande do Norte' },
  { valor: 'RS', rotulo: 'RS — Rio Grande do Sul' },
  { valor: 'RO', rotulo: 'RO — Rondônia' },
  { valor: 'RR', rotulo: 'RR — Roraima' },
  { valor: 'SC', rotulo: 'SC — Santa Catarina' },
  { valor: 'SP', rotulo: 'SP — São Paulo' },
  { valor: 'SE', rotulo: 'SE — Sergipe' },
  { valor: 'TO', rotulo: 'TO — Tocantins' },
];

export interface TelefoneDoFormulario {
  tipo: TipoDeTelefone | '';
  ddd: string;
  numero: string;
}

export interface EnderecoDoFormulario {
  nomeCurto: string;
  tipoEndereco: TipoDeEndereco | '';
  tipoLogradouro: TipoDeLogradouro | '';
  logradouro: string;
  numero: string;
  bairro: string;
  cep: string;
  cidade: string;
  uf: Uf | '';
  observacoes: string;
}

export interface CartaoDoFormulario {
  numero: string;
  nomeImpresso: string;
  /** Id da bandeira como texto, para o `<select>`; convertido para número no envio. */
  bandeiraId: string;
  /** "AAAA-MM", como o input do tipo mês entrega. */
  validade: string;
}

export interface FormularioDeCadastro {
  nome: string;
  genero: Genero | '';
  dataNascimento: string;
  cpf: string;
  email: string;
  telefone: TelefoneDoFormulario;
  senha: string;
  confirmacaoSenha: string;
  enderecos: EnderecoDoFormulario[];
  cartoes: CartaoDoFormulario[];
}

/** Erros de formato por campo, com a mesma chave usada na tela. */
export type ErrosDoCadastro = Record<string, string>;

export function enderecoVazio(): EnderecoDoFormulario {
  return {
    nomeCurto: '',
    tipoEndereco: 'CASA',
    tipoLogradouro: 'RUA',
    logradouro: '',
    numero: '',
    bairro: '',
    cep: '',
    cidade: '',
    uf: '',
    observacoes: '',
  };
}

export function cartaoVazio(): CartaoDoFormulario {
  return { numero: '', nomeImpresso: '', bandeiraId: '', validade: '' };
}

export function formularioVazio(): FormularioDeCadastro {
  return {
    nome: '',
    genero: '',
    dataNascimento: '',
    cpf: '',
    email: '',
    telefone: { tipo: 'CELULAR', ddd: '', numero: '' },
    senha: '',
    confirmacaoSenha: '',
    enderecos: [enderecoVazio()],
    cartoes: [],
  };
}

/** Remove tudo que não é dígito; CPF, CEP e telefone viajam assim para a API. */
export function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

/** CPF com máscara progressiva; aceita tanto dígitos quanto o valor já mascarado. */
export function mascararCpf(valor: string): string {
  const digitos = somenteDigitos(valor).slice(0, 11);
  const partes = [digitos.slice(0, 3), digitos.slice(3, 6), digitos.slice(6, 9)];
  let formatado = partes.filter((parte) => parte !== '').join('.');
  const verificadores = digitos.slice(9, 11);
  if (verificadores !== '') {
    formatado += `-${verificadores}`;
  }
  return formatado;
}

/** CEP com máscara progressiva ("00000-000"). */
export function formatarCep(valor: string): string {
  const digitos = somenteDigitos(valor).slice(0, 8);
  return digitos.length > 5 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : digitos;
}

/**
 * Normaliza a validade do cartão para "AAAA-MM" (DEC-07). Aceita também
 * "MM/AAAA" e "MM/AA"; devolve "" quando não reconhece o formato.
 */
export function validadeParaApi(validade: string): string {
  const texto = validade.trim();

  const anoMes = /^(\d{4})-(\d{1,2})$/.exec(texto);
  if (anoMes) {
    const [, ano, mes] = anoMes;
    if (ano === undefined || mes === undefined) {
      return '';
    }
    const numeroDoMes = Number(mes);
    if (numeroDoMes < 1 || numeroDoMes > 12) {
      return '';
    }
    return `${ano}-${String(numeroDoMes).padStart(2, '0')}`;
  }

  const mesAno = /^(\d{1,2})\/(\d{2}|\d{4})$/.exec(texto);
  if (mesAno) {
    const [, mes, ano] = mesAno;
    if (ano === undefined || mes === undefined) {
      return '';
    }
    const numeroDoMes = Number(mes);
    if (numeroDoMes < 1 || numeroDoMes > 12) {
      return '';
    }
    const anoCompleto = ano.length === 2 ? `20${ano}` : ano;
    return `${anoCompleto}-${String(numeroDoMes).padStart(2, '0')}`;
  }

  return '';
}

/** "2027-08" como "08/2027"; devolve "" quando a validade não é reconhecida. */
export function formatarValidade(validade: string): string {
  const anoMes = validadeParaApi(validade);
  if (anoMes === '') {
    return '';
  }
  const [ano, mes] = anoMes.split('-');
  if (ano === undefined || mes === undefined) {
    return '';
  }
  return `${mes}/${ano}`;
}

/** Formulário → corpo de `POST /api/clientes` (DEC-07, DEC-10). */
export function montarCadastro(formulario: FormularioDeCadastro): CadastroDeCliente {
  return {
    nome: formulario.nome.trim(),
    genero: formulario.genero as Genero,
    dataNascimento: formulario.dataNascimento.trim(),
    cpf: somenteDigitos(formulario.cpf),
    email: formulario.email.trim(),
    telefone: {
      tipo: formulario.telefone.tipo as TipoDeTelefone,
      ddd: somenteDigitos(formulario.telefone.ddd),
      numero: somenteDigitos(formulario.telefone.numero),
    },
    senha: formulario.senha,
    confirmacaoSenha: formulario.confirmacaoSenha,
    enderecos: formulario.enderecos.map(montarEndereco),
    cartoes: formulario.cartoes.map(montarCartao),
  };
}

/** Endereço do formulário no formato da API, usado no cadastro e em "Minha conta" (RN-CLI-003). */
export function montarEndereco(endereco: EnderecoDoFormulario): EnderecoDaApi {
  return {
    nomeCurto: endereco.nomeCurto.trim(),
    tipoEndereco: endereco.tipoEndereco as TipoDeEndereco,
    tipoLogradouro: endereco.tipoLogradouro as TipoDeLogradouro,
    logradouro: endereco.logradouro.trim(),
    numero: endereco.numero.trim(),
    bairro: endereco.bairro.trim(),
    cep: somenteDigitos(endereco.cep),
    cidade: endereco.cidade.trim(),
    uf: endereco.uf as Uf,
    observacoes: endereco.observacoes.trim(),
  };
}

/** Cartão do formulário no formato da API, usado no cadastro e em "Minha conta" (RN-CLI-004). */
export function montarCartao(cartao: CartaoDoFormulario): CartaoDaApi {
  return {
    numero: somenteDigitos(cartao.numero),
    nomeImpresso: cartao.nomeImpresso.trim(),
    bandeiraId: Number(cartao.bandeiraId),
    validade: validadeParaApi(cartao.validade),
  };
}

function emBranco(valor: string): boolean {
  return valor.trim() === '';
}

function validarTelefone(telefone: TelefoneDoFormulario, erros: ErrosDoCadastro): void {
  if (telefone.tipo === '') {
    erros['telefone-tipo'] = 'Informe o tipo do telefone.';
  }
  if (emBranco(telefone.ddd)) {
    erros.ddd = 'Informe o DDD.';
  }
  if (emBranco(telefone.numero)) {
    erros['telefone-numero'] = 'Informe o número do telefone.';
  }
}

function validarEndereco(
  endereco: EnderecoDoFormulario,
  indice: number,
  erros: ErrosDoCadastro,
): void {
  const chave = (campo: keyof EnderecoDoFormulario): string => `enderecos.${indice}.${campo}`;

  if (emBranco(endereco.nomeCurto)) {
    erros[chave('nomeCurto')] = 'Informe um nome curto para o endereço.';
  }
  if (endereco.tipoEndereco === '') {
    erros[chave('tipoEndereco')] = 'Informe o tipo do endereço.';
  }
  if (endereco.tipoLogradouro === '') {
    erros[chave('tipoLogradouro')] = 'Informe o tipo de logradouro.';
  }
  if (emBranco(endereco.logradouro)) {
    erros[chave('logradouro')] = 'Informe o logradouro.';
  }
  if (emBranco(endereco.numero)) {
    erros[chave('numero')] = 'Informe o número.';
  }
  if (emBranco(endereco.bairro)) {
    erros[chave('bairro')] = 'Informe o bairro.';
  }
  if (somenteDigitos(endereco.cep).length !== 8) {
    erros[chave('cep')] = emBranco(endereco.cep)
      ? 'Informe o CEP.'
      : 'Informe um CEP com 8 dígitos.';
  }
  if (emBranco(endereco.cidade)) {
    erros[chave('cidade')] = 'Informe a cidade.';
  }
  if (endereco.uf === '') {
    erros[chave('uf')] = 'Informe o estado (UF).';
  }
}

function validarCartao(
  cartao: CartaoDoFormulario,
  indice: number,
  erros: ErrosDoCadastro,
): void {
  const chave = (campo: keyof CartaoDoFormulario): string => `cartoes.${indice}.${campo}`;

  if (emBranco(cartao.numero)) {
    erros[chave('numero')] = 'Informe o número do cartão.';
  }
  if (emBranco(cartao.nomeImpresso)) {
    erros[chave('nomeImpresso')] = 'Informe o nome impresso no cartão.';
  }
  if (cartao.bandeiraId === '') {
    erros[chave('bandeiraId')] = 'Informe a bandeira do cartão.';
  }
  if (cartao.validade === '') {
    erros[chave('validade')] = 'Informe a validade do cartão.';
  }
}

/**
 * Validações de formato da tela (DEC-01): obrigatórios, CPF com 11 dígitos,
 * CEP com 8 dígitos e confirmação da senha. As regras de negócio ficam no
 * server e voltam no 422.
 */
export function validarCadastro(formulario: FormularioDeCadastro): ErrosDoCadastro {
  const erros: ErrosDoCadastro = {};

  if (emBranco(formulario.nome)) {
    erros.nome = 'Informe o nome.';
  }
  if (formulario.genero === '') {
    erros.genero = 'Informe o gênero.';
  }
  if (emBranco(formulario.dataNascimento)) {
    erros.dataNascimento = 'Informe a data de nascimento.';
  }
  if (somenteDigitos(formulario.cpf).length !== 11) {
    erros.cpf = emBranco(formulario.cpf)
      ? 'Informe o CPF.'
      : 'Informe um CPF com 11 dígitos.';
  }
  if (emBranco(formulario.email)) {
    erros.email = 'Informe o e-mail.';
  }

  validarTelefone(formulario.telefone, erros);

  if (formulario.senha === '') {
    erros.senha = 'Informe a senha.';
  }
  if (formulario.confirmacaoSenha === '') {
    erros.confirmacaoSenha = 'Confirme a senha.';
  } else if (formulario.confirmacaoSenha !== formulario.senha) {
    erros.confirmacaoSenha = 'As senhas não coincidem.';
  }

  if (formulario.enderecos.length === 0) {
    erros.enderecos = 'Informe ao menos um endereço.';
  }
  formulario.enderecos.forEach((endereco, indice) => {
    validarEndereco(endereco, indice, erros);
  });

  formulario.cartoes.forEach((cartao, indice) => {
    validarCartao(cartao, indice, erros);
  });

  return erros;
}
