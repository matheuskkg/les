import type { components } from '../../../api/esquema';
import { formatarDinheiro } from '../../../compartilhado/formatacao';
import { CapaDeLivro } from '../../../ui/CapaDeLivro';
import { PrazoDaReserva } from '../../carrinho';
import type { PagamentoConferido } from '../hooks/usePagamentoDaCompra';

type CheckoutDto = components['schemas']['CheckoutDto'];
type FreteDto = components['schemas']['FreteDto'];

interface Props {
  checkout: CheckoutDto;
  frete: FreteDto | null;
  /** Texto do frete enquanto ele não vem: sem endereço, calculando ou com problema. */
  semFrete: string;
  pagamento: PagamentoConferido | null;
}

/** Resumo fixo ao lado dos passos: itens, frete, descontos e o que falta pagar com cartão (RF-VEN-003, RF-VEN-004). */
export function ResumoDaCompra({ checkout, frete, semFrete, pagamento }: Props) {
  return (
    <aside class="cartao resumo-da-compra" aria-label="Resumo da compra">
      <h2>Resumo</h2>
      <p class="reserva">
        <PrazoDaReserva expiraEm={checkout.expiraEm} prefixo="Reserva expira em:" />
      </p>
      <ul class="itens-da-conta resumo-da-compra__itens">
        {checkout.itens.map((item) => (
          <li key={item.livroId} class="item-da-conta">
            <CapaDeLivro titulo={item.titulo ?? `Livro ${item.livroId}`} autores={item.autores} tamanho="pequena" />
            <div>
              <strong>{item.titulo ?? `Livro ${item.livroId}`}</strong>
              <br />
              <span class="campo__ajuda">
                {item.quantidade} × {item.precoUnitario ? formatarDinheiro(item.precoUnitario) : '—'}
              </span>
            </div>
            {item.subtotal && <span>{formatarDinheiro(item.subtotal)}</span>}
          </li>
        ))}
      </ul>

      <dl class="checkout__valores">
        <div class="linha-de-total">
          <dt>
            Subtotal ({checkout.quantidadeDeItens} {checkout.quantidadeDeItens === 1 ? 'item' : 'itens'})
          </dt>
          <dd>{formatarDinheiro(checkout.subtotal)}</dd>
        </div>
        <div class="linha-de-total">
          <dt>Frete</dt>
          <dd data-testid="frete">{frete ? formatarDinheiro(frete.frete) : semFrete}</dd>
        </div>
        {frete && (
          <div class="linha-de-total">
            <dt>Total</dt>
            <dd>
              <strong data-testid="total">{formatarDinheiro(frete.total)}</strong>
            </dd>
          </div>
        )}
        {pagamento && (
          <div data-testid="resumo-do-pagamento" class="resumo-da-compra__pagamento">
            {pagamento.descontoPromocional !== '0.00' && (
              <div class="linha-de-total">
                <dt>Cupom promocional</dt> <dd>− {formatarDinheiro(pagamento.descontoPromocional)}</dd>
              </div>
            )}
            {pagamento.cuponsDeTroca.map((cupom) => (
              <div class="linha-de-total" key={cupom.id}>
                <dt>Cupom {cupom.codigo}</dt> <dd>− {formatarDinheiro(cupom.abatido)}</dd>
              </div>
            ))}
            {pagamento.troco !== '0.00' && (
              <div class="linha-de-total">
                <dt>Troco em cupom de troca</dt> <dd>{formatarDinheiro(pagamento.troco)}</dd>
              </div>
            )}
            <div class="linha-de-total linha-de-total--forte">
              <dt>A pagar com cartão</dt>{' '}
              <dd>
                <strong data-testid="restante">{formatarDinheiro(pagamento.restante)}</strong>
              </dd>
            </div>
          </div>
        )}
      </dl>
      <p class="campo__ajuda">Para mudar quantidades, volte ao carrinho.</p>
    </aside>
  );
}
