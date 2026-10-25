import { useEffect, useRef } from 'preact/hooks';
import { formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao, BotaoLink } from '../../../ui/Botao';
import { Erro } from '../../../ui/Erro';
import { Violacoes } from '../../../ui/Violacoes';
import { useCarrinho } from '../contextoDoCarrinho';
import { CompraEmAndamento } from './CompraEmAndamento';
import { LinhaDoCarrinho } from './LinhaDoCarrinho';
import { PrazoDaReserva } from './PrazoDaReserva';

/** Carrinho em painel lateral, aberto pelo cabeçalho sem sair da página. Fecha com Esc ou clicando fora. */
export function GavetaDoCarrinho() {
  const { carrinho, problema, gavetaAberta, fecharGaveta, alterar, remover, irParaPagamento, iniciandoPagamento } =
    useCarrinho();
  const painel = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!gavetaAberta) {
      return;
    }
    painel.current?.focus();
    const aoTeclar = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') {
        fecharGaveta();
      }
    };
    document.addEventListener('keydown', aoTeclar);
    return () => document.removeEventListener('keydown', aoTeclar);
  }, [gavetaAberta, fecharGaveta]);

  if (!gavetaAberta) {
    return null;
  }

  const itens = carrinho?.itens ?? [];

  return (
    <div class="gaveta" onClick={(evento) => evento.target === evento.currentTarget && fecharGaveta()}>
      <div class="gaveta__painel" role="dialog" aria-modal="true" aria-labelledby="titulo-da-gaveta" tabIndex={-1} ref={painel}>
        <div class="gaveta__topo">
          <h2 id="titulo-da-gaveta">Carrinho</h2>
          <Botao variante="discreto" onClick={fecharGaveta}>
            Fechar
          </Botao>
        </div>

        {carrinho?.expiraEm && itens.length > 0 && (
          <p class="reserva">
            <PrazoDaReserva expiraEm={carrinho.expiraEm} />
          </p>
        )}

        <CompraEmAndamento aoContinuar={fecharGaveta} />

        {problema?.violacoes && problema.violacoes.length > 0 ? (
          <Violacoes violacoes={problema.violacoes} />
        ) : (
          <Erro problema={problema} />
        )}

        {carrinho === null ? (
          <p class="aviso">Carregando…</p>
        ) : itens.length === 0 ? (
          <p class="aviso">Seu carrinho está vazio.</p>
        ) : (
          <>
            <ul class="itens-da-conta">
              {itens.map((item) => (
                <LinhaDoCarrinho key={item.livroId} item={item} alterar={alterar} remover={remover} compacta />
              ))}
            </ul>
            <p class="linha-de-total linha-de-total--forte">
              <span>Subtotal</span>
              <span>{formatarDinheiro(carrinho.subtotal)}</span>
            </p>
            {carrinho.checkout ? (
              <p class="campo__ajuda">Para pagar estes itens, devolva a compra em andamento ao carrinho.</p>
            ) : (
              <Botao
                disabled={iniciandoPagamento || itens.some((item) => !item.precoUnitario)}
                onClick={() => void irParaPagamento()}
              >
                Ir para pagamento
              </Botao>
            )}
          </>
        )}
        <BotaoLink variante="secundario" href="/carrinho" onClick={fecharGaveta}>
          Ver carrinho completo
        </BotaoLink>
      </div>
    </div>
  );
}
