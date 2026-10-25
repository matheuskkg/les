/** Junta nomes de classe, ignorando os vazios. */
export function classes(...nomes: (string | false | null | undefined)[]): string {
  return nomes.filter(Boolean).join(' ');
}
