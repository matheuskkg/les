import { useLocation } from 'preact-iso';
import { useCallback, useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { Erro } from '../componentes/Erro';
import { Violacoes } from '../componentes/Violacoes';
import { useRecargaPorNotificacao } from '../contextoDeNotificacoes';
import { formatarDataHora, formatarDinheiro } from '../formatacao';

type CheckoutDto = components['schemas']['CheckoutDto'];

/**
 * Sessão de compra (RF-VEN-003, DEC-12): itens levados do carrinho, reservados até o fim do prazo, só exibido
 * (DEC-13). Para mudar quantidades, o cliente volta ao carrinho. A tela recarrega quando chega uma notificação do
 * checkout; expirado, o checkout deixa de existir e os itens ficam nos removidos do carrinho.
 */
export default function Checkout() {
  const { route } = useLocation();
  const [checkout, definirCheckout] = useState<CheckoutDto | null | undefined>(undefined);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [voltando, definirVoltando] = useState(false);

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
      {problema?.violacoes && problema.violacoes.length > 0 ? (
        <Violacoes violacoes={problema.violacoes} />
      ) : (
        <Erro problema={problema} />
      )}

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
          <p class="carrinho__total">
            Subtotal ({checkout.quantidadeDeItens} {checkout.quantidadeDeItens === 1 ? 'item' : 'itens'}):{' '}
            <strong>{formatarDinheiro(checkout.subtotal)}</strong>
          </p>
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
