import type { components } from './api/esquema';
import { montarCartao, type CartaoDoFormulario } from './cadastroDeCliente.ts';
import { escolhaParaApi, type EscolhaDeEndereco } from './escolhaDeEndereco.ts';

type PagamentoProposto = components['schemas']['PagamentoPropostoDto'];

/**
 * Cartão escolhido para pagar parte da compra (RF-VEN-006, RN-VEN-004): um cadastrado ou um novo, com o valor e o
 * código de segurança digitados. O código não vai na conferência: só na finalização, e nunca é gravado (DEC-10).
 */
export interface CartaoNaCompra {
  escolha: { cartaoId: number } | { novo: CartaoDoFormulario };
  valor: string;
  cvv: string;
}

/** "1.234,56", "1234,56" ou "1234.56" como número; `null` se não for um valor em reais. */
export function valorParaApi(valor: string): number | null {
  const texto = valor.trim().replace(/^R\$\s*/, '');
  const normalizado = texto.includes(',') ? texto.replace(/\./g, '').replace(',', '.') : texto;
  if (!/^\d+(\.\d+)?$/.test(normalizado)) {
    return null;
  }
  return Number(normalizado);
}

/** Valor da API ("49.88") como o campo mostra ("49,88"). */
export function valorParaCampo(valor: string): string {
  return valor.replace('.', ',');
}

export function cvvValido(cvv: string): boolean {
  return /^\d{3,4}$/.test(cvv.trim());
}

/** Problemas que a tela aponta antes de conferir: valor e código de segurança de cada cartão (RN-VEN-004, DEC-10). */
export function problemasDosCartoes(cartoes: ReadonlyArray<CartaoNaCompra>): string[] {
  const problemas: string[] = [];
  cartoes.forEach((cartao, indice) => {
    const rotulo = `Cartão ${indice + 1}`;
    if (valorParaApi(cartao.valor) === null) {
      problemas.push(`${rotulo}: informe o valor em reais.`);
    }
    if (!cvvValido(cartao.cvv)) {
      problemas.push(`${rotulo}: informe o código de segurança de 3 ou 4 dígitos.`);
    }
  });
  return problemas;
}

/** Corpo de `POST /api/me/checkout/pagamento`; sem `cartoes`, a resposta mostra o restante para eles. */
export function montarPagamento(
  entrega: EscolhaDeEndereco,
  promocional: string | null,
  cuponsDeTroca: ReadonlyArray<number>,
  cartoes: ReadonlyArray<CartaoNaCompra> = [],
): PagamentoProposto {
  return {
    entrega: escolhaParaApi(entrega),
    cuponsPromocionais: promocional ? [promocional] : [],
    cuponsDeTroca: [...cuponsDeTroca],
    cartoes: cartoes.map((cartao) => ({
      ...('cartaoId' in cartao.escolha
        ? { cartaoId: cartao.escolha.cartaoId }
        : { novo: montarCartao(cartao.escolha.novo) }),
      valor: valorParaApi(cartao.valor) ?? 0,
    })),
  };
}
