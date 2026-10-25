import type { components } from '../../../api/esquema';
import { PAGINA_PADRAO } from './filtrosDeLivros.ts';

export type ItemRecomendado = components['schemas']['ItemRecomendadoDto'];
export type AvaliacaoDoItem = NonNullable<ItemRecomendado['avaliacao']>;

/**
 * Aplica a avaliação do cliente na lista exibida (RNF-REC-001, DEC-03): o item avaliado negativamente some na hora,
 * sem gerar outra recomendação; o positivo fica marcado.
 */
export function aplicarAvaliacao(
  itens: readonly ItemRecomendado[],
  itemId: number,
  avaliacao: AvaliacaoDoItem,
): ItemRecomendado[] {
  if (avaliacao === 'NEGATIVA') {
    return itens.filter((item) => item.id !== itemId);
  }
  return itens.map((item) => (item.id === itemId ? { ...item, avaliacao } : item));
}

/** A seção aparece só na vitrine sem busca nem filtros, na primeira página (DEC-03). */
export function mostrarRecomendados(filtrosEmUso: number, pagina: number): boolean {
  return filtrosEmUso === 0 && pagina === PAGINA_PADRAO;
}
