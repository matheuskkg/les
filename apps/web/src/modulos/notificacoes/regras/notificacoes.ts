import type { components } from '../../../api/esquema';
import type { Papel } from '../../sessao';

/** Notificação como vem da listagem e do evento SSE `notificacao` (DEC-13). */
export type Notificacao = components['schemas']['NotificacaoDto'];
export type TipoDeNotificacao = Notificacao['tipo'];

/** Tela que uma notificação afeta e que deve ser recarregada quando ela chega (DEC-13). */
export type TelaAfetada = 'carrinho' | 'checkout' | 'pedido' | 'troca';

/** Nome do único evento do stream SSE (DEC-13). */
export const EVENTO_DO_STREAM = 'notificacao';

/** Prefixo das rotas de notificação e do stream conforme o papel (DEC-07, DEC-13). */
export function prefixoDaArea(papel: Papel): '/api/me' | '/api/admin' {
  return papel === 'ADMIN' ? '/api/admin' : '/api/me';
}

/** Endereço da página de notificações da área do usuário. */
export function paginaDeNotificacoes(papel: Papel): string {
  return papel === 'ADMIN' ? '/admin/notificacoes' : '/minha-conta/notificacoes';
}

const telasPorTipo: Record<TipoDeNotificacao, TelaAfetada> = {
  CARRINHO_EXPIRANDO: 'carrinho',
  CARRINHO_EXPIRADO: 'carrinho',
  CHECKOUT_EXPIRANDO: 'checkout',
  CHECKOUT_EXPIRADO: 'checkout',
  PEDIDO_APROVADO: 'pedido',
  PEDIDO_REPROVADO: 'pedido',
  PEDIDO_DESPACHADO: 'pedido',
  PEDIDO_ENTREGUE: 'pedido',
  PEDIDO_AGUARDANDO_DESPACHO: 'pedido',
  TROCA_AUTORIZADA: 'troca',
  TROCA_RECUSADA: 'troca',
  TROCA_CONCLUIDA: 'troca',
  TROCA_SOLICITADA: 'troca',
};

/** Tela recarregada quando chega uma notificação deste tipo. */
export function telaAfetada(tipo: TipoDeNotificacao): TelaAfetada {
  return telasPorTipo[tipo];
}

/**
 * Link da notificação a partir da referência `recurso`/`recursoId` (DEC-13), ou nulo quando ela não aponta para uma
 * tela conhecida.
 */
export function linkDaReferencia(
  papel: Papel,
  recurso: string | undefined,
  recursoId: number | undefined,
): string | null {
  const area = papel === 'ADMIN' ? '/admin' : '/minha-conta';
  switch (recurso) {
    case 'carrinho':
      return '/carrinho';
    case 'checkout':
      return '/checkout';
    case 'pedido':
      return recursoId === undefined ? `${area}/pedidos` : `${area}/pedidos/${recursoId}`;
    case 'troca':
      return recursoId === undefined ? `${area}/trocas` : `${area}/trocas/${recursoId}`;
    default:
      return null;
  }
}

/** Texto do contador do cabeçalho: nada sem pendências e "99+" acima de 99. */
export function textoDoContador(quantidade: number): string {
  if (quantidade <= 0) {
    return '';
  }
  return quantidade > 99 ? '99+' : String(quantidade);
}

/** Lê o DTO do `data` de um evento SSE, ou nulo se o corpo não tiver o formato esperado. */
export function lerNotificacaoDoEvento(dados: string): Notificacao | null {
  try {
    const valor: unknown = JSON.parse(dados);
    if (typeof valor !== 'object' || valor === null) {
      return null;
    }
    const registro = valor as Record<string, unknown>;
    const tipo = registro.tipo;
    if (typeof registro.id !== 'number' || typeof tipo !== 'string' || !(tipo in telasPorTipo)) {
      return null;
    }
    return valor as Notificacao;
  } catch {
    return null;
  }
}
