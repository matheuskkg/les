import type { paths } from '../../../api/esquema';
import { textoDoStatus } from '../../../compartilhado/formatacao.ts';
import { TAMANHO_PADRAO, normalizarPagina, normalizarTamanho } from './paginacao.ts';

/** Parâmetros de `GET /api/admin/trocas`, como definidos no contrato (RF-VEN-013, DEC-07). */
export type ConsultaDeTrocas = NonNullable<paths['/api/admin/trocas']['get']['parameters']['query']>;
export type StatusDaTroca = NonNullable<ConsultaDeTrocas['status']>[number];

/** Status do ciclo da troca, declarados aqui e não tirados de `SITUACOES_DA_TROCA` (indicador do pedido, DEC-12). */
export const STATUS_DA_TROCA: readonly { valor: StatusDaTroca; rotulo: string }[] = (
  ['EM_TROCA', 'TROCA_AUTORIZADA', 'TROCA_RECUSADA', 'TROCADO'] as const
).map((valor) => ({ valor, rotulo: textoDoStatus(valor) }));

/** Estado da tela: vários status selecionados, página (a partir de 1 na URL) e itens por página. */
export interface EstadoDasTrocas {
  status: StatusDaTroca[];
  pagina: number;
  tamanho: number;
}

/** Opções conhecidas entre as lidas, sem repetição e na ordem do ciclo, como na consulta de pedidos. */
function lerStatus(valores: string[]): StatusDaTroca[] {
  const lidos = new Set(valores);
  return STATUS_DA_TROCA.map((opcao) => opcao.valor).filter((valor) => lidos.has(valor));
}

/** Lê o estado da query string; status desconhecidos são ignorados e nenhum status marcado significa todos. */
export function lerEstadoDasTrocas(busca: string): EstadoDasTrocas {
  const parametros = new URLSearchParams(busca);
  return {
    status: lerStatus(parametros.getAll('status').map((valor) => valor.trim())),
    pagina: normalizarPagina(parametros.get('pagina')),
    tamanho: normalizarTamanho(parametros.get('tamanho')),
  };
}

/** Caminho da tela para o estado, com um parâmetro por status marcado e os padrões omitidos. */
export function urlDasTrocas(estado: EstadoDasTrocas): string {
  const parametros = new URLSearchParams();
  for (const status of estado.status) {
    parametros.append('status', status);
  }
  if (estado.tamanho !== TAMANHO_PADRAO) {
    parametros.set('tamanho', String(estado.tamanho));
  }
  if (estado.pagina > 1) {
    parametros.set('pagina', String(estado.pagina));
  }
  const texto = parametros.toString();
  return texto === '' ? '/admin/trocas' : `/admin/trocas?${texto}`;
}

/** Marca ou desmarca o valor, mantendo a ordem do ciclo (RF-VEN-013). */
export function alternarStatus(marcados: readonly StatusDaTroca[], valor: StatusDaTroca): StatusDaTroca[] {
  const selecionados = new Set(marcados);
  if (selecionados.has(valor)) {
    selecionados.delete(valor);
  } else {
    selecionados.add(valor);
  }
  return STATUS_DA_TROCA.map((opcao) => opcao.valor).filter((item) => selecionados.has(item));
}

/**
 * Consulta do contrato para o estado (RF-VEN-013, RNF-GER-001): sem status marcado não envia o parâmetro, a página vai
 * a partir de 0 e o tamanho é o escolhido na paginação.
 */
export function consultaDasTrocas(estado: EstadoDasTrocas): ConsultaDeTrocas {
  return {
    ...(estado.status.length > 0 ? { status: [...estado.status] } : {}),
    pagina: Math.max(estado.pagina - 1, 0),
    tamanho: estado.tamanho,
    ordem: ['solicitadaEm,desc'],
  };
}
