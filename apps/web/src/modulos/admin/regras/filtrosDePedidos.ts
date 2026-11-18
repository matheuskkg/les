import type { paths } from '../../../api/esquema';
import { textoDoStatus } from '../../../compartilhado/formatacao.ts';
import { TAMANHO_PADRAO, normalizarPagina, normalizarTamanho } from './paginacao.ts';

/** Parâmetros de `GET /api/admin/pedidos`, como definidos no contrato (RF-VEN-009, DEC-07). */
export type ConsultaDePedidos = NonNullable<paths['/api/admin/pedidos']['get']['parameters']['query']>;
export type StatusDoPedido = NonNullable<ConsultaDePedidos['status']>[number];
export type SituacaoDaTroca = NonNullable<ConsultaDePedidos['situacao']>[number];

/** Status do pedido na ordem do ciclo da venda, com o texto do DRS (DEC-12). */
export const STATUS: readonly { valor: StatusDoPedido; rotulo: string }[] = (
  ['EM_PROCESSAMENTO', 'APROVADA', 'REPROVADA', 'EM_TRANSPORTE', 'ENTREGUE'] as const
).map((valor) => ({ valor, rotulo: textoDoStatus(valor) }));

/** Situações do indicador de troca: o status da troca mais recente do pedido (DEC-12). */
export const SITUACOES_DA_TROCA: readonly { valor: SituacaoDaTroca; rotulo: string }[] = (
  ['EM_TROCA', 'TROCA_AUTORIZADA', 'TROCA_RECUSADA', 'TROCADO'] as const
).map((valor) => ({ valor, rotulo: textoDoStatus(valor) }));

export const ORDENS: readonly { valor: string; rotulo: string }[] = [
  { valor: 'realizadoEm,desc', rotulo: 'Mais recentes primeiro' },
  { valor: 'realizadoEm,asc', rotulo: 'Mais antigos primeiro' },
  { valor: 'total,desc', rotulo: 'Maior total' },
  { valor: 'total,asc', rotulo: 'Menor total' },
];

export const ORDEM_PADRAO = 'realizadoEm,desc';

const CAMPOS_DE_TEXTO = ['numero', 'clienteId', 'cliente', 'cpf', 'realizadoEmDe', 'realizadoEmAte'] as const;

export type CampoDeTexto = (typeof CAMPOS_DE_TEXTO)[number];

/** Filtros da consulta, guardados como vêm do formulário e da URL. */
export interface FiltrosDePedidos extends Record<CampoDeTexto, string> {
  status: StatusDoPedido[];
  situacao: SituacaoDaTroca[];
}

/** Estado da tela: filtros, ordenação, página (a partir de 1 na URL) e itens por página. */
export interface EstadoDaConsulta {
  filtros: FiltrosDePedidos;
  ordem: string;
  pagina: number;
  tamanho: number;
}

export function filtrosVazios(): FiltrosDePedidos {
  return { numero: '', clienteId: '', cliente: '', cpf: '', realizadoEmDe: '', realizadoEmAte: '', status: [], situacao: [] };
}

/** Opções conhecidas entre os valores lidos, sem repetição e na ordem da lista. */
function lerOpcoes<T extends string>(valores: string[], opcoes: readonly { valor: T }[]): T[] {
  const lidos = new Set(valores.map((valor) => valor.trim()));
  return opcoes.map((opcao) => opcao.valor).filter((valor) => lidos.has(valor));
}

/** Lê o estado da query string; valores desconhecidos são ignorados. */
export function lerEstadoDaConsulta(busca: string): EstadoDaConsulta {
  const parametros = new URLSearchParams(busca);
  const filtros = filtrosVazios();
  for (const campo of CAMPOS_DE_TEXTO) {
    filtros[campo] = (parametros.get(campo) ?? '').trim();
  }
  filtros.status = lerOpcoes(parametros.getAll('status'), STATUS);
  filtros.situacao = lerOpcoes(parametros.getAll('situacao'), SITUACOES_DA_TROCA);

  const ordem = parametros.get('ordem');
  return {
    filtros,
    ordem: ordem !== null && ORDENS.some((item) => item.valor === ordem) ? ordem : ORDEM_PADRAO,
    pagina: normalizarPagina(parametros.get('pagina')),
    tamanho: normalizarTamanho(parametros.get('tamanho')),
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
  for (const valor of estado.filtros.status) {
    parametros.append('status', valor);
  }
  for (const valor of estado.filtros.situacao) {
    parametros.append('situacao', valor);
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
  return texto === '' ? '/admin/pedidos' : `/admin/pedidos?${texto}`;
}

/** Número (do pedido ou do cliente) em branco ou só com dígitos; outro texto não vai para a API. */
export function numeroValido(numero: string): boolean {
  const texto = numero.trim();
  return texto === '' || (/^\d+$/.test(texto) && Number.isSafeInteger(Number(texto)));
}

/**
 * Consulta do contrato para o estado (RF-VEN-009): texto vazio é omitido e a página vai a partir de 0.
 * Número inválido não é enviado (a tela avisa antes, com {@link numeroValido}); intervalo invertido segue
 * para o servidor, que responde 400.
 */
export function consultaDoEstado(estado: EstadoDaConsulta): ConsultaDePedidos {
  const consulta: ConsultaDePedidos = {
    pagina: Math.max(estado.pagina - 1, 0),
    tamanho: estado.tamanho,
    ordem: [estado.ordem],
  };
  for (const campo of ['numero', 'clienteId'] as const) {
    const valor = estado.filtros[campo].trim();
    if (valor !== '' && numeroValido(valor)) {
      consulta[campo] = Number(valor);
    }
  }
  for (const campo of ['cliente', 'cpf', 'realizadoEmDe', 'realizadoEmAte'] as const) {
    const valor = estado.filtros[campo].trim();
    if (valor !== '') {
      consulta[campo] = valor;
    }
  }
  if (estado.filtros.status.length > 0) {
    consulta.status = estado.filtros.status;
  }
  if (estado.filtros.situacao.length > 0) {
    consulta.situacao = estado.filtros.situacao;
  }
  return consulta;
}
