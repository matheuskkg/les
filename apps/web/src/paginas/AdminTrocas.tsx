import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { Erro } from '../componentes/Erro';
import { useRecargaPorNotificacao } from '../contextoDeNotificacoes';
import { formatarCpf } from '../filtrosDeClientes';
import {
  OPCOES_DE_STATUS,
  consultaDasTrocas,
  lerEstadoDasTrocas,
  urlDasTrocas,
  type FiltroDeStatus,
} from '../filtrosDeTrocas';
import { formatarDataHora, textoDoStatus } from '../formatacao';

type PaginaDeTrocas = components['schemas']['PaginaTrocaListadaDto'];

interface Resultado {
  busca: string;
  pagina: PaginaDeTrocas | null;
  problema: ProblemaDetalhe | null;
}

/**
 * Trocas para o administrador, filtradas pelo status de cada troca e, sem filtro, só as `EM TROCA` (RF-VEN-013,
 * DEC-12). Cada linha leva ao pedido, onde o admin autoriza ou recusa a troca. Uma troca nova recarrega a lista.
 */
export default function AdminTrocas() {
  const { url, route } = useLocation();
  const busca = url.includes('?') ? url.slice(url.indexOf('?')) : '';
  const estado = useMemo(() => lerEstadoDasTrocas(busca), [busca]);
  const [resultado, definirResultado] = useState<Resultado | null>(null);
  const [recarga, definirRecarga] = useState(0);

  useRecargaPorNotificacao('troca', () => definirRecarga((atual) => atual + 1));

  useEffect(() => {
    const controle = new AbortController();
    cliente
      .GET('/api/admin/trocas', { params: { query: consultaDasTrocas(estado) }, signal: controle.signal })
      .then(({ data, error, response }) => {
        if (!controle.signal.aborted) {
          definirResultado(
            response.ok
              ? { busca, pagina: data ?? null, problema: null }
              : { busca, pagina: null, problema: interpretarProblema(error, response.status) },
          );
        }
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirResultado({
            busca,
            pagina: null,
            problema: { status: 0, detail: 'Não foi possível carregar as trocas. Tente novamente.' },
          });
        }
      });
    return () => controle.abort();
  }, [busca, estado, recarga]);

  const visivel = resultado?.busca === busca ? resultado : null;
  const pagina = visivel?.pagina ?? null;

  return (
    <section class="pagina">
      <header>
        <h1>Trocas</h1>
        <p class="pagina__introducao">
          Trocas pedidas pelos clientes. Abra o pedido para autorizar ou recusar uma troca em andamento.
        </p>
      </header>

      <div class="cartao">
        <div class="campo">
          <label class="campo__rotulo" for="filtro-status-da-troca">
            Status
          </label>
          <select
            id="filtro-status-da-troca"
            class="campo__entrada"
            value={estado.status}
            onChange={(evento) =>
              route(urlDasTrocas({ status: evento.currentTarget.value as FiltroDeStatus, pagina: 1 }))}
          >
            {OPCOES_DE_STATUS.map((opcao) => (
              <option key={opcao.valor} value={opcao.valor}>
                {opcao.rotulo}
              </option>
            ))}
          </select>
        </div>
      </div>

      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando trocas…
        </p>
      ) : visivel.problema ? (
        <Erro problema={visivel.problema} />
      ) : pagina !== null && pagina.itens.length === 0 && pagina.totalItens > 0 ? (
        <p class="aviso">
          Esta página não existe mais.{' '}
          <a href={urlDasTrocas({ status: estado.status, pagina: 1 })}>Ir para a primeira página</a>
        </p>
      ) : pagina === null || pagina.totalItens === 0 ? (
        <p>Nenhuma troca encontrada.</p>
      ) : (
        <>
          <div class="tabela-rolavel">
            <table class="tabela">
              <thead>
                <tr>
                  <th scope="col">Troca</th>
                  <th scope="col">Pedido</th>
                  <th scope="col">Cliente</th>
                  <th scope="col">CPF</th>
                  <th scope="col">Solicitada em</th>
                  <th scope="col">Unidades</th>
                  <th scope="col">Status</th>
                </tr>
              </thead>
              <tbody>
                {pagina.itens.map((troca) => (
                  <tr key={troca.id}>
                    <td>{troca.id}</td>
                    <td>
                      <a href={`/admin/pedidos/${troca.pedidoId}`}>{troca.pedidoId}</a>
                    </td>
                    <td>{troca.clienteNome}</td>
                    <td>{formatarCpf(troca.clienteCpf)}</td>
                    <td>{formatarDataHora(troca.solicitadaEm)}</td>
                    <td>{troca.unidades}</td>
                    <td>{textoDoStatus(troca.status)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {pagina.totalPaginas > 1 && (
            <nav class="paginacao" aria-label="Páginas de trocas">
              <button class="botao botao--secundario" type="button" disabled={estado.pagina <= 1}
                onClick={() => route(urlDasTrocas({ ...estado, pagina: estado.pagina - 1 }))}>
                Anterior
              </button>
              <span class="paginacao__posicao">
                Página {estado.pagina} de {pagina.totalPaginas}
              </span>
              <button class="botao botao--secundario" type="button" disabled={estado.pagina >= pagina.totalPaginas}
                onClick={() => route(urlDasTrocas({ ...estado, pagina: estado.pagina + 1 }))}>
                Próxima
              </button>
            </nav>
          )}
        </>
      )}
    </section>
  );
}
