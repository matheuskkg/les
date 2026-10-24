import type { components } from './api/esquema';

type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];
type ItemSolicitado = components['schemas']['ItemSolicitadoDto'];

/** Data de hoje ("2026-10-24") no fuso da loja, o mesmo do prazo de troca (RN-VEN-013). */
export function hojeNaLoja(agora: Date): string {
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Sao_Paulo',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(agora);
}

/**
 * O formulário de troca aparece para pedido entregue, dentro do prazo e com alguma unidade ainda disponível
 * (RF-VEN-011, RN-VEN-013, DEC-12). O server confere tudo de novo.
 */
export function podeSolicitarTroca(pedido: PedidoDetalhe, hoje: string): boolean {
  return (
    pedido.status === 'ENTREGUE' &&
    pedido.trocaAte !== undefined &&
    hoje <= pedido.trocaAte &&
    pedido.itens.some((item) => item.disponivelParaTroca > 0)
  );
}

/**
 * Itens marcados no formulário, com quantidade inteira maior que zero; campos vazios ou zerados ficam de fora. Uma
 * quantidade que não é número inteiro vai como está para o server responder com a mensagem da regra.
 */
export function itensDaSolicitacao(quantidades: Readonly<Record<number, string>>): ItemSolicitado[] {
  return Object.entries(quantidades).flatMap(([itemPedidoId, texto]) => {
    const limpo = texto.trim();
    if (limpo === '' || limpo === '0') {
      return [];
    }
    const quantidade = Number(limpo);
    return [{ itemPedidoId: Number(itemPedidoId), quantidade: Number.isInteger(quantidade) ? quantidade : 0 }];
  });
}
