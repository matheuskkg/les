import type { components } from '../../../api/esquema';

type ItemSolicitado = components['schemas']['ItemSolicitadoDto'];

/**
 * Itens marcados no formulário, com quantidade inteira maior que zero; campos vazios ou zerados ficam de fora. Uma
 * * quantidade que não é número inteiro vai como 0, e o server responde com a mensagem da regra.
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
