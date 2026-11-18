import type { paths } from '../../../api/esquema';
import { SITUACOES_DA_TROCA } from './filtrosDePedidos.ts';
import { TAMANHO_PADRAO, normalizarPagina, normalizarTamanho } from './paginacao.ts';

/** Parâmetros de `GET /api/admin/trocas`, como definidos no contrato (RF-VEN-013, DEC-07). */
export type ConsultaDeTrocas = NonNullable<paths['/api/admin/trocas']['get']['parameters']['query']>;
export type StatusDaTroca = NonNullable<ConsultaDeTrocas['status']>[number];

/** Filtro de status da tela: um status da troca ou todas. Sem nada na URL, mostra as trocas a autorizar. */
export type FiltroDeStatus = StatusDaTroca | 'TODAS';

export const FILTRO_PADRAO: FiltroDeStatus = 'EM_TROCA';

export const OPCOES_DE_STATUS: readonly { valor: FiltroDeStatus; rotulo: string }[] = [
  ...SITUACOES_DA_TROCA,
  { valor: 'TODAS', rotulo: 'Todas' },
];

/** Estado da tela: status, página (a partir de 1 na URL) e itens por página. */
export interface EstadoDasTrocas {
  status: FiltroDeStatus;
  pagina: number;
  tamanho: number;
}

/** Lê o estado da query string; status desconhecido volta ao padrão. */
export function lerEstadoDasTrocas(busca: string): EstadoDasTrocas {
  const parametros = new URLSearchParams(busca);
  const status = OPCOES_DE_STATUS.find((opcao) => opcao.valor === parametros.get('status')?.trim());
  return {
    status: status?.valor ?? FILTRO_PADRAO,
    pagina: normalizarPagina(parametros.get('pagina')),
    tamanho: normalizarTamanho(parametros.get('tamanho')),
  };
}

/** Caminho da tela para o estado, com os padrões omitidos. */
export function urlDasTrocas(estado: EstadoDasTrocas): string {
  const parametros = new URLSearchParams();
  if (estado.status !== FILTRO_PADRAO) {
    parametros.set('status', estado.status);
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

/** Consulta do contrato para o estado: "todas" não envia status, e a página vai a partir de 0. */
export function consultaDasTrocas(estado: EstadoDasTrocas): ConsultaDeTrocas {
  return {
    ...(estado.status === 'TODAS' ? {} : { status: [estado.status] }),
    pagina: Math.max(estado.pagina - 1, 0),
    tamanho: estado.tamanho,
    ordem: ['solicitadaEm,desc'],
  };
}
