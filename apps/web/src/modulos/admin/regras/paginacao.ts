/**
 * Regras da paginação das consultas do administrador (RNF-GER-001). O contrato do server aceita `tamanho` de 1 a 100
 * (`Paginacao.TAMANHO_MAXIMO`, `openapi.json`); a lista de opções usa valores dentro desse intervalo, com 10 como
 * padrão para deixar as linhas das tabelas mais legíveis.
 */
export const TAMANHO_MAXIMO = 100;
export const TAMANHO_PADRAO = 10;
export const TAMANHOS_DISPONIVEIS = [10, 20, 50, 100] as const;
export type TamanhoDaPagina = (typeof TAMANHOS_DISPONIVEIS)[number];

/** Rótulos do seletor de itens por página, na mesma ordem dos valores aceitos. */
export const OPCOES_DE_TAMANHO: readonly { valor: number; rotulo: string }[] = TAMANHOS_DISPONIVEIS.map((valor) => ({
  valor,
  rotulo: String(valor),
}));

function inteiro(valor: string | number | null | undefined): number | null {
  if (typeof valor === 'number') {
    return Number.isSafeInteger(valor) ? valor : null;
  }
  if (valor === null || valor === undefined) {
    return null;
  }
  const texto = valor.trim();
  if (!/^\d+$/.test(texto)) {
    return null;
  }
  const numero = Number(texto);
  return Number.isSafeInteger(numero) ? numero : null;
}

/** Tamanho válido e oferecido na tela; fora da lista ou do limite do server, volta ao padrão. */
export function normalizarTamanho(valor: string | number | null | undefined): TamanhoDaPagina {
  const numero = inteiro(valor);
  if (numero === null || numero < 1 || numero > TAMANHO_MAXIMO) {
    return TAMANHO_PADRAO;
  }
  return TAMANHOS_DISPONIVEIS.find((permitido) => permitido === numero) ?? TAMANHO_PADRAO;
}

/**
 * Página a partir de 1, como a URL a expõe. Não limita pelo total: uma página além do fim deve chegar à API e a tela
 * avisar que a página não existe mais.
 */
export function normalizarPagina(valor: string | number | null | undefined): number {
  const numero = inteiro(valor);
  return numero !== null && numero >= 1 ? numero : 1;
}
