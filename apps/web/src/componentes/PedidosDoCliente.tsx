import { useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { filtrosVazios, urlDaConsulta } from '../filtrosDePedidos';
import { formatarDataHora, formatarDinheiro, textoDoStatus } from '../formatacao';
import { Erro } from './Erro';

type PaginaDePedidos = components['schemas']['PaginaPedidoListadoDto'];

const TAMANHO = 20;

interface Resultado {
  clienteId: number;
  dados: PaginaDePedidos | null;
  problema: ProblemaDetalhe | null;
}

/**
 * Histórico de transações no cadastro do cliente, para o administrador (RF-CLI-005): os pedidos mais recentes,
 * buscados pelo código do cliente na consulta de pedidos (RF-VEN-009), com o link para a consulta completa. Não
 * usa o CPF, que uma conta encerrada libera para outro cadastro (DEC-10).
 */
export function PedidosDoCliente({ clienteId }: { clienteId: number }) {
  const [resultado, definirResultado] = useState<Resultado | null>(null);

  useEffect(() => {
    const controle = new AbortController();
    cliente
      .GET('/api/admin/pedidos', {
        params: { query: { clienteId, pagina: 0, tamanho: TAMANHO, ordem: ['realizadoEm,desc'] } },
        signal: controle.signal,
      })
      .then(({ data, error, response }) => {
        if (!controle.signal.aborted) {
          definirResultado(
            response.ok
              ? { clienteId, dados: data ?? null, problema: null }
              : { clienteId, dados: null, problema: interpretarProblema(error, response.status) },
          );
        }
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirResultado({
            clienteId,
            dados: null,
            problema: { status: 0, detail: 'Não foi possível carregar os pedidos. Tente novamente.' },
          });
        }
      });
    return () => controle.abort();
  }, [clienteId]);

  const visivel = resultado?.clienteId === clienteId ? resultado : null;
  const dados = visivel?.dados ?? null;
  const consultaCompleta = urlDaConsulta({ filtros: { ...filtrosVazios(), clienteId: String(clienteId) }, ordem: 'realizadoEm,desc', pagina: 1 });

  return (
    <div class="cartao">
      <h2>Pedidos</h2>
      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando pedidos…
        </p>
      ) : visivel.problema ? (
        <Erro problema={visivel.problema} />
      ) : dados === null || dados.totalItens === 0 ? (
        <p>Nenhum pedido realizado.</p>
      ) : (
        <>
          <div class="tabela-rolavel">
            <table class="tabela">
              <thead>
                <tr>
                  <th scope="col">Número</th>
                  <th scope="col">Realizado em</th>
                  <th scope="col">Status</th>
                  <th scope="col">Total</th>
                </tr>
              </thead>
              <tbody>
                {dados.itens.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <a href={`/admin/pedidos/${item.id}`}>{item.id}</a>
                    </td>
                    <td>{formatarDataHora(item.realizadoEm)}</td>
                    <td>{textoDoStatus(item.status)}</td>
                    <td>{formatarDinheiro(item.total)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {dados.totalItens > dados.itens.length && (
            <p>
              Mostrando os {dados.itens.length} mais recentes de {dados.totalItens}.{' '}
              <a href={consultaCompleta}>Ver todos na consulta de pedidos</a>
            </p>
          )}
        </>
      )}
    </div>
  );
}
