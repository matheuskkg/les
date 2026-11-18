/**
 * Nome acessível do botão do carrinho da navegação (ALT-12). Com o rótulo visual substituído por um ícone, é ele que
 * informa a quantidade de itens a quem usa leitor de tela.
 */
export function rotuloDoCarrinho(quantidade: number): string {
  if (quantidade <= 0) {
    return 'Carrinho';
  }
  return `Carrinho: ${quantidade} ${quantidade === 1 ? 'item' : 'itens'}`;
}
