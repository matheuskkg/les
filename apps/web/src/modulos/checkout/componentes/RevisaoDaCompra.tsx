import { formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';
import type { PagamentoDaCompra } from '../hooks/usePagamentoDaCompra';
import { Problema } from './Problema';

interface Props {
  pagamento: PagamentoDaCompra;
  /** Sem endereço de cobrança, a compra não pode ser finalizada (RF-VEN-005). */
  temCobranca: boolean;
}

/** Último passo (RF-VEN-007, RF-VEN-008): confere a divisão entre os cartões e finaliza a compra. */
export function RevisaoDaCompra({ pagamento, temCobranca }: Props) {
  const { resumo, conferindoCupons, enviando, finalizando, conferido, reprovada } = pagamento;
  return (
    <>
      <Problema problema={pagamento.problemaDosCartoes} />
      {conferido && (
        <p class="aviso aviso--sucesso" role="status">
          Pagamento conferido:{' '}
          {conferido.cartoes.length === 0
            ? 'os cupons cobrem o total.'
            : conferido.cartoes
                .map((cartao) => `${cartao.bandeira} ${cartao.numero}: ${formatarDinheiro(cartao.valor)}`)
                .join('; ')}
        </p>
      )}
      {reprovada && (
        <div class="erro" role="alert" data-testid="reprovacao">
          <p>
            O pagamento do pedido nº {reprovada.pedidoId} foi reprovado. Os cupons voltaram para você e os itens
            continuam reservados: corrija os cartões e tente de novo.
          </p>
          <ul>
            {reprovada.recusas.map((recusa, indice) => (
              <li key={indice}>
                Cartão final {recusa.ultimosDigitos}: {recusa.motivo}
              </li>
            ))}
          </ul>
        </div>
      )}
      <div class="acoes">
        <Botao
          variante="secundario"
          disabled={resumo === null || conferindoCupons || enviando || finalizando}
          onClick={pagamento.conferir}
        >
          {enviando ? 'Conferindo…' : 'Conferir pagamento'}
        </Botao>
        <Botao
          disabled={resumo === null || conferindoCupons || !temCobranca || enviando || finalizando}
          onClick={pagamento.finalizar}
        >
          {finalizando ? 'Finalizando…' : 'Finalizar compra'}
        </Botao>
      </div>
    </>
  );
}
