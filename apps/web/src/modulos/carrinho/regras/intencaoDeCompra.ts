/**
 * Livro que o visitante tentou pôr no carrinho antes de entrar. Fica guardado na aba do navegador e é
 * adicionado assim que a sessão de cliente abre, para a escolha não se perder no login.
 */
export interface IntencaoDeCompra {
  livroId: number;
  quantidade: number;
}

export const CHAVE_DA_INTENCAO = 'livraria.intencaoDeCompra';

export function serializarIntencao(intencao: IntencaoDeCompra): string {
  return JSON.stringify(intencao);
}

/** Lê a intenção guardada; qualquer valor inválido é descartado. */
export function lerIntencao(texto: string | null): IntencaoDeCompra | null {
  if (texto === null) {
    return null;
  }
  try {
    const valor: unknown = JSON.parse(texto);
    if (typeof valor !== 'object' || valor === null) {
      return null;
    }
    const { livroId, quantidade } = valor as Record<string, unknown>;
    if (
      typeof livroId === 'number' &&
      typeof quantidade === 'number' &&
      Number.isSafeInteger(livroId) &&
      Number.isSafeInteger(quantidade) &&
      livroId > 0 &&
      quantidade > 0
    ) {
      return { livroId, quantidade };
    }
    return null;
  } catch {
    return null;
  }
}
