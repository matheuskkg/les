/**
 * Regras puras do campo "Página" da paginação (RNF-GER-001). Mantidas fora do componente para serem testadas
 * isoladamente; `ui/` não depende de módulos (DEC-17).
 */

/** Máscara do campo: aceita só dígitos, inclusive no texto colado. */
export function apenasDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

/**
 * Página para a qual navegar ao confirmar o campo. O valor digitado pode vir vazio (o usuário apagou tudo), ter zeros
 * à esquerda ou passar do fim da lista; nesses casos a página é normalizada, não rejeitada:
 *
 * - sem dígitos: `null`, para o campo voltar à página atual e não navegar;
 * - `0` ou qualquer número positivo: limitado ao intervalo `1..totalPaginas`;
 * - acima do total: última página (o mesmo critério de "ir até onde dá" dos jumpers do Ant Design/Nimbus).
 */
export function paginaDaEntrada(valor: string, totalPaginas: number): number | null {
  const digitos = apenasDigitos(valor);
  if (digitos === '') {
    return null;
  }
  const maxima = Number.isSafeInteger(totalPaginas) && totalPaginas >= 1 ? totalPaginas : 1;
  const numero = Number(digitos);
  if (!Number.isFinite(numero)) {
    return maxima;
  }
  return Math.min(maxima, Math.max(1, Math.trunc(numero)));
}
