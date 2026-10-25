/** Texto sem acentos e em minúsculas, só para reconhecer a palavra "edição" em qualquer grafia. */
function semAcentos(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();
}

/**
 * Edição pronta para exibição: mantém qualquer texto que já traga "ed", "ed." ou "edição" como palavra (por exemplo,
 * "1ª edição", "2ª ed", "Edição 3" e "1ª edição revisada") e completa valores como "3ª" com " edição", sem duplicar a
 * palavra.
 */
export function textoDaEdicao(edicao: string): string {
  const texto = edicao.trim();
  if (texto === '') {
    return '';
  }
  return /\b(?:ed|edicao)\b/.test(semAcentos(texto)) ? texto : `${texto} edição`;
}
