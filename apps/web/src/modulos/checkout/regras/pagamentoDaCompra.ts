import type { components } from '../../../api/esquema';
import { montarCartao, type CartaoDoFormulario } from '../../conta/regras/cadastroDeCliente.ts';
import { escolhaParaApi, type EscolhaDeEndereco } from './escolhaDeEndereco.ts';

type PagamentoProposto = components['schemas']['PagamentoPropostoDto'];
type Finalizacao = components['schemas']['FinalizacaoDto'];

/**
 * Cartão escolhido para pagar parte da compra (RF-VEN-006, RN-VEN-004): um cadastrado ou um novo, com o valor e o
 * código de segurança digitados. O código não vai na conferência: só na finalização, e nunca é gravado (DEC-10).
 */
export interface CartaoNaCompra {
  escolha: { cartaoId: number } | { novo: CartaoDoFormulario };
  valor: string;
  cvv: string;
}

/**
 * Valor digitado em reais como o texto decimal da API ("1234.56"), sem passar por ponto flutuante; `null` se não for
 * um valor em reais. A vírgula é o separador decimal, e o ponto separa milhares ("1.234" é mil duzentos e trinta e
 * quatro); sem vírgula, um ponto seguido de 1 ou 2 dígitos também é aceito como decimal ("59.88").
 */
export function valorParaApi(valor: string): string | null {
  const texto = valor.trim().replace(/^R\$\s*/, '');
  let normalizado: string;
  if (texto.includes(',')) {
    normalizado = texto.replace(/\./g, '').replace(',', '.');
  } else if (/^\d{1,3}(\.\d{3})+$/.test(texto)) {
    normalizado = texto.replace(/\./g, '');
  } else {
    normalizado = texto;
  }
  return /^\d+(\.\d+)?$/.test(normalizado) ? normalizado : null;
}

/** Valor da API ("49.88") como o campo mostra ("49,88"). */
export function valorParaCampo(valor: string): string {
  return valor.replace('.', ',');
}

/** Texto decimal da API ("12.5") em centavos inteiros, sem ponto flutuante; `null` se não for um valor. */
export function emCentavos(valor: string): number | null {
  const encontrado = /^(\d+)(?:\.(\d{1,2}))?$/.exec(valor);
  if (!encontrado) {
    return null;
  }
  const [, reais = '0', centavos = ''] = encontrado;
  return Number(reais) * 100 + Number(centavos.padEnd(2, '0'));
}

export const MINIMO_POR_CARTAO_EM_CENTAVOS = 1000;

/**
 * Aviso do campo de valor de um cartão (RN-VEN-004): mínimo de R$ 10,00, a menos que o restante a pagar seja menor
 * que isso, quando um cartão paga o restante inteiro. Campo vazio ou ilegível não gera aviso aqui: quem aponta é
 * {@link problemasDosCartoes}.
 */
export function avisoDoValorDoCartao(valorDigitado: string, restante: string): string | null {
  const api = valorParaApi(valorDigitado);
  const valor = api === null ? null : emCentavos(api);
  const aPagar = emCentavos(restante);
  if (valor === null || aPagar === null) {
    return null;
  }
  if (valor > aPagar) {
    return 'O valor passa do que falta pagar.';
  }
  if (valor < MINIMO_POR_CARTAO_EM_CENTAVOS && aPagar >= MINIMO_POR_CARTAO_EM_CENTAVOS) {
    return 'Mínimo de R$ 10,00 por cartão.';
  }
  return null;
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
      valor: valorParaApi(cartao.valor) ?? '',
    })),
  };
}

/**
 * Corpo de `POST /api/me/checkout/finalizar` (RF-VEN-008, DEC-10): a forma de pagamento conferida, o endereço de
 * cobrança e o código de segurança de cada cartão, que segue só para a operadora.
 */
export function montarFinalizacao(
  entrega: EscolhaDeEndereco,
  cobranca: EscolhaDeEndereco,
  promocional: string | null,
  cuponsDeTroca: ReadonlyArray<number>,
  cartoes: ReadonlyArray<CartaoNaCompra>,
): Finalizacao {
  const pagamento = montarPagamento(entrega, promocional, cuponsDeTroca, cartoes);
  return {
    ...pagamento,
    entrega: escolhaParaApi(entrega),
    cobranca: escolhaParaApi(cobranca),
    cartoes: (pagamento.cartoes ?? []).map((cartao, indice) => ({
      ...cartao,
      codigoSeguranca: cartoes[indice]?.cvv.trim() ?? '',
    })),
  };
}
