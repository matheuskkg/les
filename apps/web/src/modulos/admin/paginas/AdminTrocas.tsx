import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { CampoDeSelecao } from '../../../ui/CampoDeSelecao';
import { Erro } from '../../../ui/Erro';
import { useRecargaPorNotificacao } from '../../notificacoes';
import {
  OPCOES_DE_STATUS,
  consultaDasTrocas,
  lerEstadoDasTrocas,
  urlDasTrocas,
} from '../regras/filtrosDeTrocas';
import { OPCOES_DE_TAMANHO } from '../regras/paginacao';
import { formatarCpf, formatarDataHora } from '../../../compartilhado/formatacao';
import { EtiquetaDeStatus } from '../../../ui/EtiquetaDeStatus';
import { Paginacao } from '../../../ui/Paginacao';

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
        <CampoDeSelecao
          id="filtro-status-da-troca"
          rotulo="Status"
          valor={estado.status}
          opcoes={OPCOES_DE_STATUS}
          aoMudar={(valor) => {
            const opcao = OPCOES_DE_STATUS.find((item) => item.valor === valor);
            if (opcao) {
              route(urlDasTrocas({ status: opcao.valor, pagina: 1, tamanho: estado.tamanho }));
            }
          }}
        />
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
          <a href={urlDasTrocas({ status: estado.status, pagina: 1, tamanho: estado.tamanho })}>Ir para a primeira página</a>
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
                    <td><EtiquetaDeStatus status={troca.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacao
            id="paginacao-trocas"
            pagina={estado.pagina}
            totalPaginas={pagina.totalPaginas}
            rotulo="Páginas de trocas"
            urlDaPagina={(numero) => urlDasTrocas({ ...estado, pagina: numero })}
            aoNavegar={route}
            tamanho={estado.tamanho}
            opcoesDeTamanho={OPCOES_DE_TAMANHO}
            urlDoTamanho={(tamanho) => urlDasTrocas({ ...estado, tamanho, pagina: 1 })}
          />
        </>
      )}
    </section>
  );
}
