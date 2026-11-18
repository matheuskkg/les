import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { Botao } from '../../../ui/Botao';
import { Erro } from '../../../ui/Erro';
import { OpcoesMarcaveis } from '../../../ui/OpcoesMarcaveis';
import { useRecargaPorNotificacao } from '../../notificacoes';
import {
  STATUS_DA_TROCA,
  alternarStatus,
  consultaDasTrocas,
  lerEstadoDasTrocas,
  urlDasTrocas,
  type EstadoDasTrocas,
} from '../regras/filtrosDeTrocas';
import { OPCOES_DE_TAMANHO, TAMANHO_PADRAO } from '../regras/paginacao';
import { formatarCpf, formatarDataHora } from '../../../compartilhado/formatacao';
import { buscaDaUrl, urlDoDetalhe } from '../../../compartilhado/url';
import { EtiquetaDeStatus } from '../../../ui/EtiquetaDeStatus';
import { Paginacao } from '../../../ui/Paginacao';

type PaginaDeTrocas = components['schemas']['PaginaTrocaListadaDto'];

interface Resultado {
  busca: string;
  pagina: PaginaDeTrocas | null;
  problema: ProblemaDetalhe | null;
}

/**
 * Trocas para o administrador, com filtro por vários status ao mesmo tempo (RF-VEN-013, DEC-12), como na consulta de
 * pedidos. Sem status marcado, mostra todas as trocas. Cada linha leva ao pedido, onde o admin autoriza ou recusa a
 * troca. Uma troca nova recarrega a lista.
 */
export default function AdminTrocas() {
  const { url, route } = useLocation();
  const busca = buscaDaUrl(url);
  const estado = useMemo(() => lerEstadoDasTrocas(busca), [busca]);
  const [filtros, definirFiltros] = useState<EstadoDasTrocas>(estado);
  const [resultado, definirResultado] = useState<Resultado | null>(null);
  const [recarga, definirRecarga] = useState(0);

  useRecargaPorNotificacao('troca', () => definirRecarga((atual) => atual + 1));

  useEffect(() => {
    definirFiltros(estado);
  }, [estado]);

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

  function filtrar(evento: Event) {
    evento.preventDefault();
    route(urlDasTrocas({ ...estado, status: filtros.status, pagina: 1, tamanho: estado.tamanho }));
  }

  return (
    <section class="pagina">
      <header>
        <h1>Trocas</h1>
        <p class="pagina__introducao">
          Trocas pedidas pelos clientes. Abra o pedido para autorizar ou recusar uma troca em andamento.
        </p>
      </header>

      <form class="cartao filtros filtros-admin" role="search" aria-label="Filtrar trocas" onSubmit={filtrar}>
        <fieldset class="filtros-admin__grupo">
          <legend class="filtros-admin__legenda">Status da troca</legend>
          <div class="filtros-admin__campos filtros-admin__campos--opcoes">
            <OpcoesMarcaveis
              legenda="Marcar um ou mais status"
              nome="status"
              opcoes={STATUS_DA_TROCA}
              marcados={filtros.status}
              alternar={(valor) => definirFiltros((atual) => ({
                ...atual,
                status: alternarStatus(atual.status, valor),
              }))}
            />
          </div>
        </fieldset>
        <div class="acoes filtros-admin__acoes">
          <Botao type="submit">Filtrar</Botao>
          <Botao
            variante="secundario"
            type="button"
            onClick={() => {
              definirFiltros({ status: [], pagina: 1, tamanho: TAMANHO_PADRAO });
              route('/admin/trocas');
            }}
          >
            Limpar filtros
          </Botao>
        </div>
      </form>

      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando trocas…
        </p>
      ) : visivel.problema ? (
        <Erro problema={visivel.problema} />
      ) : pagina !== null && pagina.itens.length === 0 && pagina.totalItens > 0 ? (
        <p class="aviso">
          Esta página não existe mais.{' '}
          <a href={urlDasTrocas({ ...estado, pagina: 1 })}>Ir para a primeira página</a>
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
                      <a href={urlDoDetalhe(`/admin/pedidos/${troca.pedidoId}`, urlDasTrocas(estado))}>
                        {troca.pedidoId}
                      </a>
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
