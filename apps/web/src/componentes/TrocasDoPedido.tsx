import type { ComponentChildren } from 'preact';
import type { components } from '../api/esquema';
import { formatarData, formatarDataHora, formatarDinheiro, textoDoStatus } from '../formatacao';

type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];
type Troca = components['schemas']['TrocaDto'];

/**
 * Indicador de troca do pedido e todas as trocas dele, da mais recente à mais antiga (DEC-12). O status do pedido
 * continua o da venda; a troca aparece ao lado, com a situação da mais recente e se ela é parcial ou total. O admin
 * passa `acoes` para autorizar ou recusar cada troca (RF-VEN-012).
 */
export function TrocasDoPedido({
  pedido,
  acoes,
}: {
  pedido: PedidoDetalhe;
  acoes?: (troca: Troca) => ComponentChildren;
}) {
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
                {acoes && <th scope="col">Ações</th>}
              </tr>
            </thead>
            <tbody>
              {pedido.trocas.map((troca) => (
                <tr key={troca.id} data-testid={`troca-${troca.id}`}>
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
                  {acoes && <td>{acoes(troca)}</td>}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
