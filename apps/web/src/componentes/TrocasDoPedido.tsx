import type { components } from '../api/esquema';
import { formatarData, formatarDataHora, formatarDinheiro, textoDoStatus } from '../formatacao';

type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];

/**
 * Indicador de troca do pedido e todas as trocas dele, da mais recente à mais antiga (DEC-12). O status do pedido
 * continua o da venda; a troca aparece ao lado, com a situação da mais recente e se ela é parcial ou total.
 */
export function TrocasDoPedido({ pedido }: { pedido: PedidoDetalhe }) {
  if (pedido.trocas.length === 0 && pedido.trocaAte === undefined) {
    return null;
  }
  const tituloDoItem = new Map(pedido.itens.map((item) => [item.id, item.livro.titulo]));

  return (
    <div class="cartao">
      <h2>Trocas</h2>
      <dl class="detalhe__campos">
        {pedido.troca && (
          <div>
            <dt>Troca</dt>
            <dd data-testid="indicador-de-troca">
              {textoDoStatus(pedido.troca.situacao)} ({pedido.troca.abrangencia === 'TOTAL' ? 'total' : 'parcial'})
            </dd>
          </div>
        )}
        {pedido.trocaAte && (
          <div><dt>Troca até</dt><dd>{formatarData(pedido.trocaAte)}</dd></div>
        )}
      </dl>
      {pedido.trocas.length === 0 ? (
        <p>Nenhuma troca solicitada.</p>
      ) : (
        <div class="tabela-rolavel">
          <table class="tabela" data-testid="trocas-do-pedido">
            <thead>
              <tr>
                <th scope="col">Número</th>
                <th scope="col">Solicitada em</th>
                <th scope="col">Status</th>
                <th scope="col">Itens</th>
                <th scope="col">Cupom</th>
              </tr>
            </thead>
            <tbody>
              {pedido.trocas.map((troca) => (
                <tr key={troca.id}>
                  <td>{troca.id}</td>
                  <td>{formatarDataHora(troca.solicitadaEm)}</td>
                  <td>
                    {textoDoStatus(troca.status)}
                    {troca.motivoRecusa && (
                      <>
                        <br />
                        Motivo: {troca.motivoRecusa}
                      </>
                    )}
                  </td>
                  <td>
                    {troca.itens
                      .map((item) => `${item.quantidade} × ${tituloDoItem.get(item.itemPedidoId) ?? 'item ' + item.itemPedidoId}`)
                      .join('; ')}
                  </td>
                  <td>{troca.valorCupom ? formatarDinheiro(troca.valorCupom) : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
