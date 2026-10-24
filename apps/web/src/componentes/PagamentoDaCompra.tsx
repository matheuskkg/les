import { useCallback, useEffect, useRef, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { cartaoVazio, montarCartao, type CartaoDoFormulario } from '../cadastroDeCliente';
import type { EscolhaDeEndereco } from '../escolhaDeEndereco';
import { formatarData, formatarDinheiro } from '../formatacao';
import {
  montarPagamento,
  problemasDosCartoes,
  valorParaCampo,
  type CartaoNaCompra,
} from '../pagamentoDaCompra';
import { CampoDeFormulario } from './CampoDeFormulario';
import { CamposDoCartao } from './CamposDoCartao';
import { Erro } from './Erro';
import { Violacoes } from './Violacoes';

type CartaoCadastrado = components['schemas']['CartaoDaResposta'];
type CupomDeTroca = components['schemas']['CupomDeTrocaDto'];
type PagamentoConferido = components['schemas']['PagamentoConferidoDto'];

interface Props {
  checkoutId: number;
  /** Endereço de entrega, que define o frete; sem ele, o pagamento não é conferido. */
  entrega: EscolhaDeEndereco | null;
  /** Chamado quando a API responde que não há checkout aberto, para a tela recarregá-lo. */
  aoPerderCheckout: () => void;
}

/** Valor e código de segurança digitados para um cartão, pela chave do cartão na tela. */
interface Digitado {
  valor: string;
  cvv: string;
}

const NOVO = 'novo';

/** Mensagem do 404 de checkout inexistente; os demais 404 (endereço, cupom, cartão) são mostrados ao cliente. */
export const SEM_CHECKOUT = 'Nenhuma compra em andamento.';

function Problema({ problema }: { problema: ProblemaDetalhe | null }) {
  return problema?.violacoes && problema.violacoes.length > 0 ? (
    <Violacoes violacoes={problema.violacoes} />
  ) : (
    <Erro problema={problema} />
  );
}

/**
 * Forma de pagamento (RF-VEN-006, RF-VEN-007, DEC-11): um cupom promocional pelo código, os cupons de troca do
 * cliente e os cartões, cadastrados ou novos, cada um com o valor e o código de segurança. A distribuição é conferida
 * a cada mudança dos cupons, o que já avisa de cupom desnecessário (RN-VEN-006); os cartões, ao conferir o pagamento.
 */
export function PagamentoDaCompra({ checkoutId, entrega, aoPerderCheckout }: Props) {
  const [cartoes, definirCartoes] = useState<CartaoCadastrado[]>([]);
  const [cupons, definirCupons] = useState<CupomDeTroca[]>([]);
  const [codigo, definirCodigo] = useState('');
  const [promocional, definirPromocional] = useState<string | null>(null);
  const [selecionados, definirSelecionados] = useState<number[]>([]);
  const [resumo, definirResumo] = useState<PagamentoConferido | null>(null);
  const [problemaDosCupons, definirProblemaDosCupons] = useState<ProblemaDetalhe | null>(null);
  /** Chaves dos cartões em uso, na ordem em que foram escolhidos: o id do cadastrado ou {@link NOVO}. */
  const [emUso, definirEmUso] = useState<string[]>([]);
  const [digitados, definirDigitados] = useState<Record<string, Digitado>>({});
  const [novo, definirNovo] = useState<CartaoDoFormulario>(cartaoVazio);
  const [conferido, definirConferido] = useState<PagamentoConferido | null>(null);
  const [problemaDosCartoes, definirProblemaDosCartoes] = useState<ProblemaDetalhe | null>(null);
  const [enviando, definirEnviando] = useState(false);
  const [salvando, definirSalvando] = useState(false);
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

  // RN-VEN-006: a distribuição sem cartões mostra o restante e já recusa cupons desnecessários.
  useEffect(() => {
    selecao.current += 1;
    definirResumo(null);
    definirConferido(null);
    if (entrega === null) {
      definirProblemaDosCupons(null);
      return;
    }
    let ativo = true;
    void (async () => {
      const { data, error, response } = await cliente.POST('/api/me/checkout/pagamento', {
        body: montarPagamento(entrega, promocional, selecionados),
      });
      if (!ativo) {
        return;
      }
      const problema = data ? null : interpretarProblema(error, response.status);
      if (problema?.status === 404 && problema.detail === SEM_CHECKOUT) {
        aoPerderCheckout();
        return;
      }
      definirProblemaDosCupons(problema);
      definirResumo(data ?? null);
    })().catch(() => {
      if (ativo) {
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

  async function conferir() {
    if (entrega === null || resumo === null || enviando) {
      return;
    }
    // Com os cupons cobrindo tudo, os cartões escolhidos antes ficam fora (DEC-11: restante zero não aceita cartão).
    const naCompra = resumo.restante === '0.00' ? [] : cartoesNaCompra();
    const versao = selecao.current;
    const problemas = problemasDosCartoes(naCompra);
    if (problemas.length > 0) {
      definirProblemaDosCartoes({
        status: 422,
        detail: 'Confira os cartões.',
        violacoes: problemas.map((mensagem) => ({ codigo: 'RN-VEN-004', mensagem })),
      });
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

  function camposDoPagamento(chave: string) {
    return (
      <div class="grade-do-cadastro">
        <CampoDeFormulario id={`cartao-${chave}-valor`} rotulo="Valor neste cartão (R$)" inputMode="decimal" required
          valor={digitado(chave).valor} aoMudar={(valor) => digitar(chave, 'valor', valor)} />
        <CampoDeFormulario id={`cartao-${chave}-cvv`} rotulo="Código de segurança" inputMode="numeric" required
          autoComplete="cc-csc" maxLength={4} valor={digitado(chave).cvv}
          aoMudar={(valor) => digitar(chave, 'cvv', valor)} />
      </div>
    );
  }

  return (
    <section class="pagamento-da-compra" aria-labelledby="titulo-pagamento">
      <h2 id="titulo-pagamento">Forma de pagamento</h2>

      <form
        class="pagamento-da-compra__promocional"
        noValidate
        onSubmit={(evento) => {
          evento.preventDefault();
          definirPromocional(codigo.trim() === '' ? null : codigo.trim().toUpperCase());
        }}
      >
        <CampoDeFormulario id="cupom-promocional" rotulo="Cupom promocional" valor={codigo}
          aoMudar={definirCodigo} ajuda="Um cupom promocional por compra." />
        <div class="acoes">
          <button class="botao botao--secundario" type="submit">Aplicar cupom</button>
          {promocional && (
            <button class="botao botao--secundario" type="button"
              onClick={() => {
                definirPromocional(null);
                definirCodigo('');
              }}>
              Retirar {promocional}
            </button>
          )}
        </div>
      </form>

      {cupons.length > 0 && (
        <fieldset class="grupo-de-categorias escolha-de-endereco">
          <legend>Cupons de troca</legend>
          {cupons.map((cupom) => (
            <label class="categoria-opcao" key={cupom.id}>
              <input type="checkbox" checked={selecionados.includes(cupom.id)} onChange={() => alternarCupom(cupom.id)} />
              <span>
                {cupom.codigo} — {formatarDinheiro(cupom.valor)}
                {cupom.validade && ` (vale até ${formatarData(cupom.validade)})`}
              </span>
            </label>
          ))}
        </fieldset>
      )}
      <Problema problema={problemaDosCupons} />

      {resumo && (
        <dl class="checkout__valores" data-testid="resumo-do-pagamento">
          {resumo.descontoPromocional !== '0.00' && (
            <div>
              <dt>Desconto do cupom promocional:</dt> <dd>− {formatarDinheiro(resumo.descontoPromocional)}</dd>
            </div>
          )}
          {resumo.cuponsDeTroca.map((cupom) => (
            <div key={cupom.id}>
              <dt>Cupom {cupom.codigo}:</dt> <dd>− {formatarDinheiro(cupom.abatido)}</dd>
            </div>
          ))}
          {resumo.troco !== '0.00' && (
            <div>
              <dt>Troco, gerado como cupom de troca:</dt> <dd>{formatarDinheiro(resumo.troco)}</dd>
            </div>
          )}
          <div class="carrinho__total">
            <dt>A pagar com cartão:</dt>{' '}
            <dd>
              <strong data-testid="restante">{formatarDinheiro(resumo.restante)}</strong>
            </dd>
          </div>
        </dl>
      )}

      {resumo && resumo.restante !== '0.00' && (
        <fieldset class="grupo-de-categorias escolha-de-endereco">
          <legend>Cartões</legend>
          {cartoes.map((cartao) => {
            const chave = String(cartao.id);
            return (
              <div key={cartao.id}>
                <label class="categoria-opcao">
                  <input type="checkbox" checked={emUso.includes(chave)} onChange={() => alternarCartao(chave)} />
                  <span>
                    {cartao.bandeira.nome} {cartao.numeroMascarado}
                    {cartao.preferencial && ' (preferencial)'}
                  </span>
                </label>
                {emUso.includes(chave) && camposDoPagamento(chave)}
              </div>
            );
          })}
          <label class="categoria-opcao">
            <input type="checkbox" checked={emUso.includes(NOVO)} onChange={() => alternarCartao(NOVO)} />
            Outro cartão
          </label>
          {emUso.includes(NOVO) && (
            <div class="escolha-de-endereco__novo">
              <CamposDoCartao prefixo="cartao-novo" formulario={novo}
                aoMudar={(campo, valor) => {
                  definirConferido(null);
                  definirNovo((atual) => ({ ...atual, [campo]: valor }));
                }} />
              {camposDoPagamento(NOVO)}
              <div class="acoes">
                <button class="botao botao--secundario" type="button" disabled={salvando}
                  onClick={() => void salvarNovo()}>
                  Salvar no meu cadastro
                </button>
              </div>
            </div>
          )}
          <p class="campo__ajuda">
            Mínimo de R$ 10,00 por cartão, exceto quando cupons deixam menos que isso a pagar. O código de segurança não
            fica gravado.
          </p>
        </fieldset>
      )}

      <Problema problema={problemaDosCartoes} />
      {conferido && (
        <p class="aviso" role="status">
          Pagamento conferido:{' '}
          {conferido.cartoes.length === 0
            ? 'os cupons cobrem o total.'
            : conferido.cartoes
                .map((cartao) => `${cartao.bandeira} ${cartao.numero}: ${formatarDinheiro(cartao.valor)}`)
                .join('; ')}
        </p>
      )}
      <div class="acoes">
        <button class="botao" type="button" disabled={resumo === null || enviando} onClick={() => void conferir()}>
          {enviando ? 'Conferindo…' : 'Conferir pagamento'}
        </button>
      </div>
    </section>
  );
}
