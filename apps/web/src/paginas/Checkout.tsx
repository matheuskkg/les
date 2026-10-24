import { useLocation } from 'preact-iso';
import { useCallback, useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { montarEndereco, type EnderecoDoFormulario } from '../cadastroDeCliente';
import { Erro } from '../componentes/Erro';
import { SelecaoDeEndereco } from '../componentes/SelecaoDeEndereco';
import { Violacoes } from '../componentes/Violacoes';
import { useRecargaPorNotificacao } from '../contextoDeNotificacoes';
import { escolhaInicial, escolhaParaApi, type EscolhaDeEndereco } from '../escolhaDeEndereco';
import { formatarDataHora, formatarDinheiro } from '../formatacao';

type CheckoutDto = components['schemas']['CheckoutDto'];
type EnderecoCadastrado = components['schemas']['EnderecoDaResposta'];
type FreteDto = components['schemas']['FreteDto'];

function Problema({ problema }: { problema: ProblemaDetalhe | null }) {
  return problema?.violacoes && problema.violacoes.length > 0 ? (
    <Violacoes violacoes={problema.violacoes} />
  ) : (
    <Erro problema={problema} />
  );
}

/**
 * Sessão de compra (RF-VEN-003, DEC-12): itens levados do carrinho, reservados até o fim do prazo, só exibido
 * (DEC-13). Para mudar quantidades, o cliente volta ao carrinho. A tela recarrega quando chega uma notificação do
 * checkout; expirado, o checkout deixa de existir e os itens ficam nos removidos do carrinho.
 *
 * Endereços de entrega e de cobrança (RF-VEN-005, DEC-10): cadastrados ou novos, com a opção de salvar no cadastro; a
 * cobrança vem igual à entrega. O frete é recalculado a cada troca do endereço de entrega (RF-VEN-004, DEC-05.3).
 */
export default function Checkout() {
  const { route } = useLocation();
  const [checkout, definirCheckout] = useState<CheckoutDto | null | undefined>(undefined);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [voltando, definirVoltando] = useState(false);
  const [enderecos, definirEnderecos] = useState<EnderecoCadastrado[] | null>(null);
  const [entrega, definirEntrega] = useState<EscolhaDeEndereco | null>(null);
  const [cobrancaIgual, definirCobrancaIgual] = useState(true);
  const [cobranca, definirCobranca] = useState<EscolhaDeEndereco | null>(null);
  const [problemaDoEndereco, definirProblemaDoEndereco] = useState<ProblemaDetalhe | null>(null);
  const [frete, definirFrete] = useState<FreteDto | null>(null);

  const carregar = useCallback(async () => {
    const { data, error, response } = await cliente.GET('/api/me/checkout');
    if (response.status === 404) {
      definirCheckout(null);
      return;
    }
    if (!data) {
      definirProblema(interpretarProblema(error, response.status));
      return;
    }
    definirCheckout(data);
  }, []);

  useEffect(() => {
    void carregar().catch(() =>
      definirProblema({ status: 0, detail: 'Não foi possível carregar a compra. Tente novamente.' }),
    );
  }, [carregar]);

  useRecargaPorNotificacao('checkout', () => void carregar().catch(() => undefined));

  const carregarEnderecos = useCallback(async () => {
    const { data, error, response } = await cliente.GET('/api/me/enderecos');
    if (!data) {
      definirProblemaDoEndereco(interpretarProblema(error, response.status));
      return null;
    }
    definirEnderecos(data);
    return data;
  }, []);

  const checkoutId = checkout?.id;
  const carregarEscolhaInicial = useCallback(() => {
    definirProblemaDoEndereco(null);
    void carregarEnderecos()
      .then((lidos) => {
        if (lidos) {
          const inicial = escolhaInicial(lidos);
          definirEntrega(inicial);
          definirCobranca(inicial);
        }
      })
      .catch(() =>
        definirProblemaDoEndereco({ status: 0, detail: 'Não foi possível carregar os endereços. Tente novamente.' }),
      );
  }, [carregarEnderecos]);

  useEffect(() => {
    if (checkoutId !== undefined) {
      carregarEscolhaInicial();
    }
  }, [checkoutId, carregarEscolhaInicial]);

  // RF-VEN-004: o frete depende só do endereço de entrega e dos itens do checkout.
  useEffect(() => {
    definirFrete(null);
    if (checkoutId === undefined || entrega === null) {
      definirProblemaDoEndereco(null);
      return;
    }
    let ativo = true;
    void (async () => {
      const { data, error, response } = await cliente.POST('/api/me/checkout/frete', {
        body: { entrega: escolhaParaApi(entrega) },
      });
      if (!ativo) {
        return;
      }
      if (!data) {
        definirProblemaDoEndereco(interpretarProblema(error, response.status));
        return;
      }
      definirProblemaDoEndereco(null);
      definirFrete(data);
    })().catch(() => {
      if (ativo) {
        definirProblemaDoEndereco({ status: 0, detail: 'Não foi possível calcular o frete. Tente novamente.' });
      }
    });
    return () => {
      ativo = false;
    };
  }, [checkoutId, entrega]);

  /** Usa um endereço novo; com `salvar`, grava no cadastro (RF-CLI-006) e passa a usá-lo pelo id. */
  async function usarNovo(
    formulario: EnderecoDoFormulario,
    salvar: boolean,
    definirEscolha: (escolha: EscolhaDeEndereco) => void,
  ): Promise<boolean> {
    definirProblemaDoEndereco(null);
    if (!salvar) {
      definirEscolha({ novo: formulario });
      return true;
    }
    try {
      const { data, error, response } = await cliente.POST('/api/me/enderecos', { body: montarEndereco(formulario) });
      if (!data) {
        definirProblemaDoEndereco(interpretarProblema(error, response.status));
        return false;
      }
      // Já gravado: entra na lista sem nova leitura, para que uma falha dela não leve a gravar de novo.
      definirEnderecos((atuais) => [...(atuais ?? []), data]);
      definirEscolha({ enderecoId: data.id });
      return true;
    } catch {
      definirProblemaDoEndereco({ status: 0, detail: 'Não foi possível salvar o endereço. Tente novamente.' });
      return false;
    }
  }

  async function voltar() {
    definirVoltando(true);
    definirProblema(null);
    try {
      const { data, error, response } = await cliente.POST('/api/me/checkout/voltar');
      if (!data) {
        definirProblema(interpretarProblema(error, response.status));
        await carregar();
        return;
      }
      route('/carrinho');
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível voltar ao carrinho. Tente novamente.' });
    } finally {
      definirVoltando(false);
    }
  }

  return (
    <section class="pagina">
      <h1>Pagamento</h1>
      <Problema problema={problema} />

      {checkout === undefined ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : checkout === null ? (
        <div class="cartao">
          <p>Nenhuma compra em andamento.</p>
          <a class="botao" href="/carrinho">
            Ir para o carrinho
          </a>
        </div>
      ) : (
        <div class="cartao">
          <ul class="itens-da-conta">
            {checkout.itens.map((item) => (
              <li key={item.livroId} class="item-da-conta">
                <div>
                  <strong>{item.titulo ?? `Livro ${item.livroId}`}</strong>
                  <br />
                  {item.autores.join('; ')}
                  {item.precoUnitario && ` · ${formatarDinheiro(item.precoUnitario)} cada`}
                </div>
                <div class="item-da-conta__acoes">
                  <span>
                    {item.quantidade} {item.quantidade === 1 ? 'unidade' : 'unidades'}
                  </span>
                  {item.subtotal && <strong>{formatarDinheiro(item.subtotal)}</strong>}
                </div>
              </li>
            ))}
          </ul>
          {enderecos !== null && (
            <>
              <SelecaoDeEndereco
                legenda="Endereço de entrega"
                prefixo="entrega"
                enderecos={enderecos}
                escolha={entrega}
                aoEscolher={definirEntrega}
                aoUsarNovo={(formulario, salvar) => usarNovo(formulario, salvar, definirEntrega)}
              />
              <label class="categoria-opcao">
                <input
                  type="checkbox"
                  checked={cobrancaIgual}
                  onChange={() => {
                    // DEC-10: a cobrança separada começa igual à entrega atual.
                    if (cobrancaIgual) {
                      definirCobranca(entrega);
                    }
                    definirCobrancaIgual(!cobrancaIgual);
                  }}
                />
                Cobrança no mesmo endereço da entrega
              </label>
              {!cobrancaIgual && (
                <SelecaoDeEndereco
                  legenda="Endereço de cobrança"
                  prefixo="cobranca"
                  enderecos={enderecos}
                  escolha={cobranca}
                  aoEscolher={definirCobranca}
                  aoUsarNovo={(formulario, salvar) => usarNovo(formulario, salvar, definirCobranca)}
                />
              )}
            </>
          )}
          <Problema problema={problemaDoEndereco} />
          {enderecos === null && problemaDoEndereco !== null && (
            <div class="acoes">
              <button class="botao botao--secundario" type="button" onClick={carregarEscolhaInicial}>
                Carregar os endereços de novo
              </button>
            </div>
          )}

          <dl class="checkout__valores">
            <div>
              <dt>Subtotal ({checkout.quantidadeDeItens} {checkout.quantidadeDeItens === 1 ? 'item' : 'itens'}):</dt>{' '}
              <dd>{formatarDinheiro(checkout.subtotal)}</dd>
            </div>
            <div>
              <dt>Frete:</dt>{' '}
              <dd data-testid="frete">
                {frete ? formatarDinheiro(frete.frete) : entrega === null
                    ? 'escolha o endereço de entrega'
                    : problemaDoEndereco
                      ? '—'
                      : 'calculando…'}
              </dd>
            </div>
            {frete && (
              <div class="carrinho__total">
                <dt>Total:</dt>{' '}
                <dd>
                  <strong data-testid="total">{formatarDinheiro(frete.total)}</strong>
                </dd>
              </div>
            )}
          </dl>
          <p class="campo__ajuda">
            Os itens ficam reservados até {formatarDataHora(checkout.expiraEm)}. Para mudar quantidades, volte ao
            carrinho.
          </p>
          <div class="acoes carrinho__acoes">
            <button
              class="botao botao--secundario"
              type="button"
              disabled={voltando}
              onClick={() => void voltar()}
            >
              Voltar ao carrinho
            </button>
          </div>
        </div>
      )}
    </section>
  );
}
