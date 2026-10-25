import { useCallback, useEffect, useRef, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { cartaoVazio, montarCartao, type CartaoDoFormulario } from '../../conta/regras/cadastroDeCliente';
import type { EscolhaDeEndereco } from '../regras/escolhaDeEndereco';
import {
  excedenteEmCentavos as calcularExcedente,
  faltaPagarEmCentavos as calcularFaltaPagar,
  montarFinalizacao,
  montarPagamento,
  problemasDosCartoes,
  valorParaCampo,
  type CartaoNaCompra,
} from '../regras/pagamentoDaCompra';

export type CartaoCadastrado = components['schemas']['CartaoDaResposta'];
export type CupomDeTroca = components['schemas']['CupomDeTrocaDto'];
export type PagamentoConferido = components['schemas']['PagamentoConferidoDto'];
export type CompraFinalizada = components['schemas']['CompraFinalizadaDto'];

interface Props {
  checkoutId: number;
  /** Endereço de entrega, que define o frete; sem ele, o pagamento não é conferido. */
  entrega: EscolhaDeEndereco | null;
  /** Endereço de cobrança, exigido na finalização (RF-VEN-005). */
  cobranca: EscolhaDeEndereco | null;
  /** Chamado quando a API responde que não há checkout aberto, para a tela recarregá-lo. */
  aoPerderCheckout: () => void;
  /** Chamado com o pedido aprovado; o checkout foi concluído (DEC-12). */
  aoAprovar: (compra: CompraFinalizada) => void;
  /** Avisa quando a finalização começa e termina, para a tela travar o que pode encerrar o checkout. */
  aoMudarFinalizacao: (finalizando: boolean) => void;
}

/** Valor e código de segurança digitados para um cartão, pela chave do cartão na tela. */
export interface Digitado {
  valor: string;
  cvv: string;
}

/** Chave do cartão novo, digitado na compra. */
export const NOVO = 'novo';

/** Mensagem do 404 de checkout inexistente; os demais 404 (endereço, cupom, cartão) são mostrados ao cliente. */
export const SEM_CHECKOUT = 'Nenhuma compra em andamento.';


/**
 * Estado da forma de pagamento (RF-VEN-006, RF-VEN-007, DEC-11): um cupom promocional pelo código, os cupons de troca
 * do cliente e os cartões, cadastrados ou novos, cada um com o valor e o código de segurança. A distribuição é
 * conferida a cada mudança dos cupons, o que já avisa de cupom desnecessário (RN-VEN-006); os cartões, ao conferir.
 *
 * A finalização (RF-VEN-008, DEC-13) envia tudo com o código de segurança. Na reprovação (RN-VEN-008), os cartões
 * recusados aparecem com o motivo e o checkout continua aberto para outra tentativa.
 */
export function usePagamentoDaCompra({
  checkoutId,
  entrega,
  cobranca,
  aoPerderCheckout,
  aoAprovar,
  aoMudarFinalizacao,
}: Props) {
  const [cartoes, definirCartoes] = useState<CartaoCadastrado[]>([]);
  const [cupons, definirCupons] = useState<CupomDeTroca[]>([]);
  const [codigo, definirCodigo] = useState('');
  const [promocional, definirPromocional] = useState<string | null>(null);
  const [selecionados, definirSelecionados] = useState<number[]>([]);
  const [resumo, definirResumo] = useState<PagamentoConferido | null>(null);
  const [conferindoCupons, definirConferindoCupons] = useState(false);
  const [problemaDosCupons, definirProblemaDosCupons] = useState<ProblemaDetalhe | null>(null);
  /** Chaves dos cartões em uso, na ordem em que foram escolhidos: o id do cadastrado ou {@link NOVO}. */
  const [emUso, definirEmUso] = useState<string[]>([]);
  const [digitados, definirDigitados] = useState<Record<string, Digitado>>({});
  const [novo, definirNovo] = useState<CartaoDoFormulario>(cartaoVazio);
  const [conferido, definirConferido] = useState<PagamentoConferido | null>(null);
  const [problemaDosCartoes, definirProblemaDosCartoes] = useState<ProblemaDetalhe | null>(null);
  const [enviando, definirEnviando] = useState(false);
  const [salvando, definirSalvando] = useState(false);
  const [finalizando, definirFinalizando] = useState(false);
  const [reprovada, definirReprovada] = useState<CompraFinalizada | null>(null);
  /** Muda a cada mudança de cupons ou endereço, para descartar uma conferência que chegou atrasada. */
  const selecao = useRef(0);

  const carregarCartoes = useCallback(async () => {
    const { data } = await cliente.GET('/api/me/cartoes');
    if (data) {
      definirCartoes(data);
    }
    return data ?? null;
  }, []);

  useEffect(() => {
    void Promise.all([carregarCartoes(), cliente.GET('/api/me/cupons-de-troca')])
      .then(([, resposta]) => definirCupons(resposta.data ?? []))
      .catch(() =>
        definirProblemaDosCartoes({ status: 0, detail: 'Não foi possível carregar cartões e cupons. Tente novamente.' }),
      );
  }, [carregarCartoes]);

  // RN-VEN-006: a distribuição sem cartões mostra o restante e já recusa cupons desnecessários. O resumo anterior fica
  // na tela até o novo chegar, mas não serve para conferir nem finalizar.
  useEffect(() => {
    selecao.current += 1;
    definirReprovada(null);
    definirConferido(null);
    if (entrega === null) {
      definirResumo(null);
      definirConferindoCupons(false);
      definirProblemaDosCupons(null);
      return;
    }
    let ativo = true;
    definirConferindoCupons(true);
    void (async () => {
      const { data, error, response } = await cliente.POST('/api/me/checkout/pagamento', {
        body: montarPagamento(entrega, promocional, selecionados),
      });
      if (!ativo) {
        return;
      }
      definirConferindoCupons(false);
      const problema = data ? null : interpretarProblema(error, response.status);
      if (problema?.status === 404 && problema.detail === SEM_CHECKOUT) {
        aoPerderCheckout();
        return;
      }
      definirProblemaDosCupons(problema);
      definirResumo(data ?? null);
    })().catch(() => {
      if (ativo) {
        definirConferindoCupons(false);
        definirResumo(null);
        definirProblemaDosCupons({ status: 0, detail: 'Não foi possível conferir os cupons. Tente novamente.' });
      }
    });
    return () => {
      ativo = false;
    };
  }, [checkoutId, entrega, promocional, selecionados, aoPerderCheckout]);

  // Com um cartão só, ele paga todo o restante: acompanha a mudança dos cupons (RN-VEN-004).
  const unico = emUso.length === 1 ? emUso[0] : undefined;
  const restante = resumo?.restante;
  useEffect(() => {
    if (unico !== undefined && restante !== undefined) {
      definirDigitados((atuais) => ({
        ...atuais,
        [unico]: { cvv: atuais[unico]?.cvv ?? '', valor: valorParaCampo(restante) },
      }));
    }
  }, [unico, restante]);

  function digitado(chave: string): Digitado {
    return digitados[chave] ?? { valor: '', cvv: '' };
  }

  function digitar(chave: string, campo: keyof Digitado, valor: string) {
    definirConferido(null);
    definirReprovada(null);
    definirDigitados((atuais) => ({ ...atuais, [chave]: { ...digitado(chave), ...atuais[chave], [campo]: valor } }));
  }

  /** O primeiro cartão escolhido já vem com o restante; a divisão entre vários fica com o cliente (DEC-11). */
  function alternarCartao(chave: string) {
    definirConferido(null);
    if (emUso.includes(chave)) {
      definirEmUso(emUso.filter((atual) => atual !== chave));
      return;
    }
    if (emUso.length === 0 && resumo && digitado(chave).valor === '') {
      digitar(chave, 'valor', valorParaCampo(resumo.restante));
    }
    definirEmUso([...emUso, chave]);
  }

  function alternarCupom(id: number) {
    definirSelecionados((atuais) => (atuais.includes(id) ? atuais.filter((atual) => atual !== id) : [...atuais, id]));
  }

  function cartoesNaCompra(): CartaoNaCompra[] {
    return emUso.map((chave) => ({
      escolha: chave === NOVO ? { novo } : { cartaoId: Number(chave) },
      ...digitado(chave),
    }));
  }

  async function salvarNovo() {
    if (salvando) {
      return;
    }
    definirSalvando(true);
    definirProblemaDosCartoes(null);
    try {
      const { data, error, response } = await cliente.POST('/api/me/cartoes', { body: montarCartao(novo) });
      if (!data) {
        definirProblemaDosCartoes(interpretarProblema(error, response.status));
        return;
      }
      // Já gravado: entra na lista sem depender de outra leitura, levando o que foi digitado para ele.
      definirCartoes((atuais) => [...atuais, data]);
      const chave = String(data.id);
      definirDigitados((atuais) => ({ ...atuais, [chave]: digitado(NOVO) }));
      definirEmUso((atuais) => atuais.map((atual) => (atual === NOVO ? chave : atual)));
      definirNovo(cartaoVazio());
      void carregarCartoes().catch(() => undefined);
    } catch {
      definirProblemaDosCartoes({ status: 0, detail: 'Não foi possível salvar o cartão. Tente novamente.' });
    } finally {
      definirSalvando(false);
    }
  }

  /** Cartões que pagam o restante, ou `null` se algum tiver problema, já apontado na tela. */
  function cartoesConferidosNaTela(restante: string): CartaoNaCompra[] | null {
    // Com os cupons cobrindo tudo, os cartões escolhidos antes ficam fora (DEC-11: restante zero não aceita cartão).
    const naCompra = restante === '0.00' ? [] : cartoesNaCompra();
    const problemas = problemasDosCartoes(naCompra);
    if (problemas.length > 0) {
      definirProblemaDosCartoes({
        status: 422,
        detail: 'Confira os cartões.',
        violacoes: problemas.map((mensagem) => ({ codigo: 'RN-VEN-004', mensagem })),
      });
      return null;
    }
    return naCompra;
  }

  async function conferir() {
    if (entrega === null || resumo === null || conferindoCupons || enviando || finalizando) {
      return;
    }
    const versao = selecao.current;
    const naCompra = cartoesConferidosNaTela(resumo.restante);
    if (naCompra === null) {
      return;
    }
    definirEnviando(true);
    definirProblemaDosCartoes(null);
    try {
      const { data, error, response } = await cliente.POST('/api/me/checkout/pagamento', {
        body: montarPagamento(entrega, promocional, selecionados, naCompra),
      });
      if (versao !== selecao.current) {
        return;
      }
      if (!data) {
        definirProblemaDosCartoes(interpretarProblema(error, response.status));
        return;
      }
      definirConferido(data);
    } catch {
      definirProblemaDosCartoes({ status: 0, detail: 'Não foi possível conferir o pagamento. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  async function finalizar() {
    if (entrega === null || cobranca === null || resumo === null || conferindoCupons || enviando || finalizando) {
      return;
    }
    const naCompra = cartoesConferidosNaTela(resumo.restante);
    if (naCompra === null) {
      return;
    }
    definirFinalizando(true);
    aoMudarFinalizacao(true);
    definirProblemaDosCartoes(null);
    definirReprovada(null);
    try {
      const { data, error, response } = await cliente.POST('/api/me/checkout/finalizar', {
        body: montarFinalizacao(entrega, cobranca, promocional, selecionados, naCompra),
      });
      if (!data) {
        const problema = interpretarProblema(error, response.status);
        if (problema.status === 404 && problema.detail === SEM_CHECKOUT) {
          aoPerderCheckout();
          return;
        }
        definirProblemaDosCartoes(problema);
        return;
      }
      if (data.status === 'APROVADA') {
        aoAprovar(data);
        return;
      }
      if (data.recusas.length === 0) {
        // O prazo da compra acabou antes da aprovação: o pedido foi reprovado e o checkout não existe mais.
        aoPerderCheckout();
        return;
      }
      definirReprovada(data);
    } catch {
      definirProblemaDosCartoes({
        status: 0,
        detail: 'Não foi possível finalizar a compra. Confira em instantes se o pedido foi registrado.',
      });
    } finally {
      definirFinalizando(false);
      aoMudarFinalizacao(false);
    }
  }

  function aplicarPromocional(texto: string) {
    definirPromocional(texto.trim() === '' ? null : texto.trim().toUpperCase());
  }

  function retirarPromocional() {
    definirPromocional(null);
    definirCodigo('');
  }

  function mudarNovo(campo: keyof CartaoDoFormulario, valor: string) {
    definirConferido(null);
    definirNovo((atual) => ({ ...atual, [campo]: valor }));
  }

  /** Quanto falta pagar com os cartões em uso, já descontando o que foi digitado (RN-VEN-004). */
  const valoresEmUso = emUso.map((chave) => digitado(chave).valor);
  const faltaPagar =
    resumo === null ? null : calcularFaltaPagar(resumo.restante, valoresEmUso);
  /** Quanto os cartões passam do restante; independe da ordem de escolha (RN-VEN-004). */
  const excedente = resumo === null ? 0 : calcularExcedente(resumo.restante, valoresEmUso);
  /** O servidor libera a exceção do mínimo quando a compra usa algum cupom (RN-VEN-005). */
  const comCupom = promocional !== null || selecionados.length > 0;

  return {
    cartoes,
    cupons,
    codigo,
    definirCodigo,
    promocional,
    aplicarPromocional,
    retirarPromocional,
    selecionados,
    alternarCupom,
    resumo,
    /** Os cupons estão sendo conferidos de novo; até lá, {@link resumo} é o da seleção anterior. */
    conferindoCupons,
    problemaDosCupons,
    emUso,
    alternarCartao,
    digitado,
    digitar,
    faltaPagar,
    excedente,
    comCupom,
    novo,
    mudarNovo,
    salvarNovo: () => void salvarNovo(),
    salvando,
    conferido,
    problemaDosCartoes,
    enviando,
    finalizando,
    reprovada,
    conferir: () => void conferir(),
    finalizar: () => void finalizar(),
  };
}

export type PagamentoDaCompra = ReturnType<typeof usePagamentoDaCompra>;
