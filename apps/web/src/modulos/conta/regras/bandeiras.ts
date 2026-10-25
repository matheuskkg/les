/**
 * Bandeiras de cartão de crédito (RN-CLI-005). Espelha a tabela `bandeira` do
 * seed (DEC-16): a API não tem endpoint para listá-las.
 */
export const BANDEIRAS: ReadonlyArray<{ id: number; nome: string }> = [
  { id: 1, nome: 'Visa' },
  { id: 2, nome: 'Mastercard' },
  { id: 3, nome: 'Elo' },
  { id: 4, nome: 'American Express' },
  { id: 5, nome: 'Hipercard' },
];
