import { Botao, BotaoLink } from '../../../ui/Botao';
import { useCarrinho } from '../contextoDoCarrinho';
import { PrazoDaReserva } from './PrazoDaReserva';

/**
 * Checkout aberto do cliente (DEC-12): os itens saíram do carrinho e seguem reservados até o fim do prazo. Daqui o
 * cliente volta ao pagamento ou devolve a compra ao carrinho, somando os itens aos que já estiverem lá.
 */
export function CompraEmAndamento({ aoContinuar }: { aoContinuar?: () => void }) {
  const { carrinho, devolverCompra, devolvendoCompra } = useCarrinho();
  const compra = carrinho?.checkout;
  if (!compra) {
    return null;
  }
  const itens = compra.quantidadeDeItens === 1 ? '1 item reservado' : `${compra.quantidadeDeItens} itens reservados`;

  return (
    <div class="compra-em-andamento" data-testid="compra-em-andamento">
      <p>
        <strong>Você tem uma compra em andamento</strong>, com {itens}.{' '}
        <PrazoDaReserva expiraEm={compra.expiraEm} prefixo="Expira em" />
      </p>
      <div class="acoes">
        <BotaoLink href="/checkout" onClick={aoContinuar}>
          Continuar pagamento
        </BotaoLink>
        <Botao variante="secundario" disabled={devolvendoCompra} onClick={() => void devolverCompra()}>
          Devolver ao carrinho
        </Botao>
      </div>
    </div>
  );
}
