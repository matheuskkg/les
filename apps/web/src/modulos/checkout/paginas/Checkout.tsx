import { useLocation } from 'preact-iso';
import { useCallback, useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { formatarDataHora, formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao, BotaoLink } from '../../../ui/Botao';
import { Esqueleto } from '../../../ui/Esqueleto';
import { useRecargaPorNotificacao } from '../../notificacoes';
import { CartoesDaCompra } from '../componentes/CartoesDaCompra';
import { CuponsDaCompra } from '../componentes/CuponsDaCompra';
import { IndicadorDePassos, PassoDaCompra } from '../componentes/PassoDaCompra';
import { Problema } from '../componentes/Problema';
import { ResumoDaCompra } from '../componentes/ResumoDaCompra';
import { RevisaoDaCompra } from '../componentes/RevisaoDaCompra';
import { SelecaoDeEndereco } from '../componentes/SelecaoDeEndereco';
import { useEntregaDaCompra } from '../hooks/useEntregaDaCompra';
import { usePagamentoDaCompra, type CompraFinalizada } from '../hooks/usePagamentoDaCompra';
import { avisoDoValorDoCartao } from '../regras/pagamentoDaCompra';

type CheckoutDto = components['schemas']['CheckoutDto'];

function PedidoAprovado({ compra }: { compra: CompraFinalizada }) {
  return (
    <div class="cartao pedido-aprovado" data-testid="pedido-aprovado">
      <p class="aviso aviso--sucesso" role="status">
        Pedido nº {compra.pedidoId} realizado em {formatarDataHora(compra.realizadoEm)}: pagamento de{' '}
        {formatarDinheiro(compra.total)} aprovado.
      </p>
      <div class="acoes">
        <BotaoLink href={`/minha-conta/pedidos/${compra.pedidoId}`}>Acompanhar pedido</BotaoLink>
        <BotaoLink variante="secundario" href="/">
          Continuar comprando
        </BotaoLink>
      </div>
    </div>
  );
}

/**
 * Sessão de compra (RF-VEN-003, DEC-12) em quatro passos — entrega, cupons, cartões e revisão — com o resumo fixo ao
 * lado. Os itens vêm do carrinho e ficam reservados até o fim do prazo (DEC-13); para mudar quantidades, o cliente
 * volta ao carrinho. A tela recarrega quando chega uma notificação do checkout; expirado, o checkout deixa de existir e
 * os itens ficam nos removidos do carrinho. Aprovado o pedido, a tela mostra o número dele.
 */
export default function Checkout() {
  const { route } = useLocation();
  const [checkout, definirCheckout] = useState<CheckoutDto | null | undefined>(undefined);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [voltando, definirVoltando] = useState(false);
  const [aprovada, definirAprovada] = useState<CompraFinalizada | null>(null);
  const [finalizando, definirFinalizando] = useState(false);

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

  // Estável entre renderizações: é dependência dos efeitos que conferem frete e pagamento.
  const recarregar = useCallback(() => void carregar().catch(() => undefined), [carregar]);
  useRecargaPorNotificacao('checkout', recarregar);

  const entrega = useEntregaDaCompra(checkout?.id, recarregar);
  const pagamento = usePagamentoDaCompra({
    checkoutId: checkout?.id ?? 0,
    entrega: checkout ? entrega.entrega : null,
    cobranca: entrega.cobranca,
    aoPerderCheckout: recarregar,
    aoAprovar: definirAprovada,
    aoMudarFinalizacao: definirFinalizando,
  });

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

  if (aprovada) {
    return (
      <section class="pagina pagina--estreita">
        <h1>Pagamento</h1>
        <PedidoAprovado compra={aprovada} />
      </section>
    );
  }

  const { resumo } = pagamento;
  const entregaPronta = entrega.frete !== null && entrega.cobranca !== null;
  const cartoesProntos =
    resumo !== null &&
    (resumo.restante === '0.00' ||
      (pagamento.emUso.length > 0 &&
        pagamento.emUso.every((chave) => {
          const { valor, cvv } = pagamento.digitado(chave);
          return valor !== '' && cvv !== '' && avisoDoValorDoCartao(valor, resumo.restante) === null;
        })));
  const cuponsUsados = (pagamento.promocional ? 1 : 0) + pagamento.selecionados.length;
  const passos = [
    { titulo: 'Entrega', concluido: entregaPronta },
    { titulo: 'Cupons', concluido: entregaPronta && resumo !== null && pagamento.problemaDosCupons === null },
    { titulo: 'Cartões', concluido: cartoesProntos },
    { titulo: 'Revisão', concluido: false },
  ];
  const semFrete =
    entrega.entrega === null ? 'escolha o endereço de entrega' : entrega.problema ? '—' : 'calculando…';

  return (
    <section class="pagina checkout">
      <h1>Pagamento</h1>
      <Problema problema={problema} />

      {checkout === undefined ? (
        <Esqueleto linhas={6} rotulo="Carregando a compra…" />
      ) : checkout === null ? (
        <div class="cartao">
          <p>Nenhuma compra em andamento.</p>
          <BotaoLink href="/carrinho">Ir para o carrinho</BotaoLink>
        </div>
      ) : (
        <>
          <IndicadorDePassos passos={passos} />
          <div class="checkout__corpo">
            <div class="checkout__passos">
              <PassoDaCompra numero={1} titulo="Entrega" concluido={passos[0]?.concluido}>
                {entrega.enderecos === null ? (
                  entrega.problema === null ? (
                    <Esqueleto linhas={2} rotulo="Carregando os endereços…" />
                  ) : (
                    <div class="acoes">
                      <Botao variante="secundario" onClick={entrega.carregarEscolhaInicial}>
                        Carregar os endereços de novo
                      </Botao>
                    </div>
                  )
                ) : (
                  <>
                    <SelecaoDeEndereco
                      legenda="Endereço de entrega"
                      prefixo="entrega"
                      enderecos={entrega.enderecos}
                      escolha={entrega.entrega}
                      aoEscolher={entrega.definirEntrega}
                      aoUsarNovo={entrega.usarNovoNaEntrega}
                    />
                    <label class="categoria-opcao">
                      <input
                        type="checkbox"
                        checked={entrega.cobrancaIgual}
                        onChange={entrega.alternarCobrancaIgual}
                      />
                      Cobrança no mesmo endereço da entrega
                    </label>
                    {!entrega.cobrancaIgual && (
                      <SelecaoDeEndereco
                        legenda="Endereço de cobrança"
                        prefixo="cobranca"
                        enderecos={entrega.enderecos}
                        escolha={entrega.cobrancaSeparada}
                        aoEscolher={entrega.definirCobranca}
                        aoUsarNovo={entrega.usarNovoNaCobranca}
                      />
                    )}
                  </>
                )}
                <Problema problema={entrega.problema} />
              </PassoDaCompra>

              {/* Enquanto finaliza, nada do pagamento muda: a tela continua igual ao que foi enviado. */}
              <fieldset class="checkout__pagamento" disabled={finalizando}>
                <PassoDaCompra
                  numero={2}
                  titulo="Cupons"
                  concluido={passos[1]?.concluido}
                  resumo={cuponsUsados > 0 ? `${cuponsUsados} aplicado${cuponsUsados === 1 ? '' : 's'}` : null}
                >
                  <CuponsDaCompra pagamento={pagamento} />
                </PassoDaCompra>
                <PassoDaCompra numero={3} titulo="Cartões" concluido={passos[2]?.concluido}>
                  <CartoesDaCompra pagamento={pagamento} />
                </PassoDaCompra>
              </fieldset>

              <PassoDaCompra numero={4} titulo="Revisão">
                <RevisaoDaCompra pagamento={pagamento} temCobranca={entrega.cobranca !== null} />
                <div class="acoes">
                  <Botao variante="discreto" disabled={voltando || finalizando} onClick={() => void voltar()}>
                    Voltar ao carrinho
                  </Botao>
                </div>
              </PassoDaCompra>
            </div>

            <ResumoDaCompra checkout={checkout} frete={entrega.frete} semFrete={semFrete} pagamento={resumo} />
          </div>
        </>
      )}
    </section>
  );
}
