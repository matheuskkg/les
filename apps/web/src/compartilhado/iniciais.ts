/** Iniciais para o avatar: primeira letra do primeiro e do último nome ("Ana Souza" → "AS"). */
export function iniciais(nome: string): string {
  const partes = nome.trim().split(/[\s@.]+/).filter((parte) => parte !== '');
  const primeira = partes[0]?.[0] ?? '';
  const ultima = partes.length > 1 ? (partes[partes.length - 1]?.[0] ?? '') : '';
  return (primeira + ultima).toUpperCase() || '?';
}
