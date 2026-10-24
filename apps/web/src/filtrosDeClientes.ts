import type { paths } from './api/esquema';

/** Parâmetros de `GET /api/admin/clientes`, como definidos no contrato (RF-CLI-004, DEC-07). */
export type ConsultaDeClientes = NonNullable<paths['/api/admin/clientes']['get']['parameters']['query']>;
export type Genero = NonNullable<ConsultaDeClientes['genero']>[number];
export type Situacao = NonNullable<ConsultaDeClientes['situacao']>[number];
export type Uf = NonNullable<ConsultaDeClientes['uf']>[number];

export const GENEROS: readonly { valor: Genero; rotulo: string }[] = [
  { valor: 'FEMININO', rotulo: 'Feminino' },
  { valor: 'MASCULINO', rotulo: 'Masculino' },
  { valor: 'OUTRO', rotulo: 'Outro' },
  { valor: 'NAO_INFORMADO', rotulo: 'Não informado' },
];

export const SITUACOES: readonly { valor: Situacao; rotulo: string }[] = [
  { valor: 'ATIVA', rotulo: 'Ativa' },
  { valor: 'INATIVA', rotulo: 'Inativa' },
  { valor: 'ENCERRADA', rotulo: 'Encerrada' },
];

export const UFS: readonly Uf[] = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
];

export const ORDENS: readonly { valor: string; rotulo: string }[] = [
  { valor: 'nome,asc', rotulo: 'Nome A–Z' },
  { valor: 'nome,desc', rotulo: 'Nome Z–A' },
  { valor: 'dataNascimento,asc', rotulo: 'Mais velhos primeiro' },
  { valor: 'dataNascimento,desc', rotulo: 'Mais novos primeiro' },
];

export const ORDEM_PADRAO = 'nome,asc';
export const TAMANHO_DA_PAGINA = 20;

const CAMPOS_DE_TEXTO = [
  'nome',
  'cpf',
  'email',
  'telefone',
  'cidade',
  'dataNascimentoDe',
  'dataNascimentoAte',
] as const;

export type CampoDeTexto = (typeof CAMPOS_DE_TEXTO)[number];

/** Filtros da consulta, guardados como vêm do formulário e da URL. */
export interface FiltrosDeClientes extends Record<CampoDeTexto, string> {
  genero: Genero[];
  situacao: Situacao[];
  uf: Uf[];
}

/** Estado da tela: filtros, ordenação e página (a partir de 1 na URL). */
export interface EstadoDaConsulta {
  filtros: FiltrosDeClientes;
  ordem: string;
  pagina: number;
}

export function filtrosVazios(): FiltrosDeClientes {
  return {
    nome: '',
    cpf: '',
    email: '',
    telefone: '',
    cidade: '',
    dataNascimentoDe: '',
    dataNascimentoAte: '',
    genero: [],
    situacao: [],
    uf: [],
  };
}

function somenteValidos<T extends string>(valores: string[], permitidos: readonly T[]): T[] {
  const unicos = new Set<T>();
  for (const valor of valores) {
    const permitido = permitidos.find((item) => item === valor.trim());
    if (permitido !== undefined) {
      unicos.add(permitido);
    }
  }
  return [...unicos];
}

/** Lê o estado da query string; valores desconhecidos são ignorados. */
export function lerEstadoDaConsulta(busca: string): EstadoDaConsulta {
  const parametros = new URLSearchParams(busca);
  const filtros = filtrosVazios();
  for (const campo of CAMPOS_DE_TEXTO) {
    filtros[campo] = (parametros.get(campo) ?? '').trim();
  }
  filtros.genero = somenteValidos(parametros.getAll('genero'), GENEROS.map((item) => item.valor));
  filtros.situacao = somenteValidos(parametros.getAll('situacao'), SITUACOES.map((item) => item.valor));
  filtros.uf = somenteValidos(parametros.getAll('uf'), UFS);

  const ordem = parametros.get('ordem');
  const pagina = Number.parseInt(parametros.get('pagina') ?? '', 10);
  return {
    filtros,
    ordem: ORDENS.some((item) => item.valor === ordem) && ordem !== null ? ordem : ORDEM_PADRAO,
    pagina: Number.isSafeInteger(pagina) && pagina > 1 ? pagina : 1,
  };
}

/** Caminho da consulta para o estado, com os padrões omitidos. */
export function urlDaConsulta(estado: EstadoDaConsulta): string {
  const parametros = new URLSearchParams();
  for (const campo of CAMPOS_DE_TEXTO) {
    const valor = estado.filtros[campo].trim();
    if (valor !== '') {
      parametros.append(campo, valor);
    }
  }
  for (const campo of ['genero', 'situacao', 'uf'] as const) {
    for (const valor of estado.filtros[campo]) {
      parametros.append(campo, valor);
    }
  }
  if (estado.ordem !== ORDEM_PADRAO) {
    parametros.set('ordem', estado.ordem);
  }
  if (estado.pagina > 1) {
    parametros.set('pagina', String(estado.pagina));
  }
  const texto = parametros.toString();
  return texto === '' ? '/admin/clientes' : `/admin/clientes?${texto}`;
}

/**
 * Consulta do contrato para o estado (RF-CLI-004): texto vazio é omitido e a página
 * vai a partir de 0. Intervalo invertido segue para o servidor, que responde 400.
 */
export function consultaDoEstado(estado: EstadoDaConsulta): ConsultaDeClientes {
  const consulta: ConsultaDeClientes = {
    pagina: Math.max(estado.pagina - 1, 0),
    tamanho: TAMANHO_DA_PAGINA,
    ordem: [estado.ordem],
  };
  for (const campo of CAMPOS_DE_TEXTO) {
    const valor = estado.filtros[campo].trim();
    if (valor !== '') {
      consulta[campo] = valor;
    }
  }
  if (estado.filtros.genero.length > 0) {
    consulta.genero = estado.filtros.genero;
  }
  if (estado.filtros.situacao.length > 0) {
    consulta.situacao = estado.filtros.situacao;
  }
  if (estado.filtros.uf.length > 0) {
    consulta.uf = estado.filtros.uf;
  }
  return consulta;
}

/** CPF só com dígitos exibido como 000.000.000-00; outro formato volta como veio. */
export function formatarCpf(cpf: string): string {
  const digitos = cpf.replace(/\D/g, '');
  if (digitos.length !== 11) {
    return cpf;
  }
  return `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
}

/** Rótulo de exibição de um valor de enum, ou o próprio valor se não houver. */
export function rotuloDe<T extends string>(
  opcoes: readonly { valor: T; rotulo: string }[],
  valor: T,
): string {
  return opcoes.find((opcao) => opcao.valor === valor)?.rotulo ?? valor;
}
