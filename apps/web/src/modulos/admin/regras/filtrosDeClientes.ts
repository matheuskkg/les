import type { paths } from '../../../api/esquema';
import { TAMANHO_PADRAO, normalizarPagina, normalizarTamanho } from './paginacao.ts';

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

/** Estado da tela: filtros, ordenação, página (a partir de 1 na URL) e itens por página. */
export interface EstadoDaConsulta {
  filtros: FiltrosDeClientes;
  ordem: string;
  pagina: number;
  tamanho: number;
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
  return {
    filtros,
    ordem: ORDENS.some((item) => item.valor === ordem) && ordem !== null ? ordem : ORDEM_PADRAO,
    pagina: normalizarPagina(parametros.get('pagina')),
    tamanho: normalizarTamanho(parametros.get('tamanho')),
  };
}

/** Query string canônica do estado da consulta, começando por `?` ou vazia. */
export function buscaDaConsulta(estado: EstadoDaConsulta): string {
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
  if (estado.tamanho !== TAMANHO_PADRAO) {
    parametros.set('tamanho', String(estado.tamanho));
  }
  if (estado.pagina > 1) {
    parametros.set('pagina', String(estado.pagina));
  }
  const texto = parametros.toString();
  return texto === '' ? '' : `?${texto}`;
}

/** Caminho da consulta para o estado, com os padrões omitidos. */
export function urlDaConsulta(estado: EstadoDaConsulta): string {
  return `/admin/clientes${buscaDaConsulta(estado)}`;
}

/**
 * Leva os filtros aplicados para a URL do detalhe do cliente, de modo que o retorno à consulta não os perca
 * (RF-CLI-004). A busca é normalizada: valores desconhecidos e padrões não aparecem na URL do detalhe.
 */
export function urlDoCliente(id: string | number, busca: string): string {
  return `/admin/clientes/${id}${buscaDaConsulta(lerEstadoDaConsulta(busca))}`;
}

/** Recompõe a consulta a partir da busca preservada na URL do detalhe (RF-CLI-004). */
export function urlDeVoltaAosClientes(busca: string): string {
  return `/admin/clientes${buscaDaConsulta(lerEstadoDaConsulta(busca))}`;
}

/**
 * Consulta do contrato para o estado (RF-CLI-004): texto vazio é omitido e a página
 * vai a partir de 0. Intervalo invertido segue para o servidor, que responde 400.
 */
export function consultaDoEstado(estado: EstadoDaConsulta): ConsultaDeClientes {
  const consulta: ConsultaDeClientes = {
    pagina: Math.max(estado.pagina - 1, 0),
    tamanho: estado.tamanho,
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

/** Rótulo de exibição de um valor de enum, ou o próprio valor se não houver. */
export function rotuloDe<T extends string>(
  opcoes: readonly { valor: T; rotulo: string }[],
  valor: T,
): string {
  return opcoes.find((opcao) => opcao.valor === valor)?.rotulo ?? valor;
}
