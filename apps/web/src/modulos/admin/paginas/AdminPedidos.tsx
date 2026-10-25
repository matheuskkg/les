import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { Erro } from '../../../ui/Erro';
import { OpcoesMarcaveis } from '../../../ui/OpcoesMarcaveis';
import {
  ORDENS,
  SITUACOES_DA_TROCA,
  STATUS,
  consultaDoEstado,
  filtrosVazios,
  lerEstadoDaConsulta,
  numeroValido,
  urlDaConsulta,
  type CampoDeTexto,
  type FiltrosDePedidos,
} from '../regras/filtrosDePedidos';
import { formatarCpf, formatarDataHora, formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';
import { EtiquetaDeStatus } from '../../../ui/EtiquetaDeStatus';
import { Paginacao } from '../../../ui/Paginacao';
import { SeletorDeOrdem } from '../../../ui/SeletorDeOrdem';
import { useCarregamento } from '../../../compartilhado/useCarregamento';
import { AbasDeStatus } from '../componentes/AbasDeStatus';

type PaginaDePedidos = components['schemas']['PaginaPedidoListadoDto'];

interface Resultado {
  busca: string;
  pagina: PaginaDePedidos | null;
  problema: ProblemaDetalhe | null;
}

const NUMERO_INVALIDO = 'O número do pedido deve ter só dígitos.';
const CODIGO_INVALIDO = 'O código do cliente deve ter só dígitos.';

/** Marca ou desmarca o valor, mantendo a ordem da lista de opções. */
function alternarNaOrdem<T extends string>(opcoes: readonly { valor: T }[], marcados: T[], valor: T): T[] {
  return marcados.includes(valor)
    ? marcados.filter((item) => item !== valor)
    : opcoes.map((item) => item.valor).filter((item) => item === valor || marcados.includes(item));
}

/**
 * Consulta de pedidos do administrador, com filtros combinados guardados na URL (RF-VEN-009, RF-VEN-010). A situação
 * da troca filtra pelo status da troca mais recente do pedido (DEC-12).
 */
export default function AdminPedidos() {
  const { url, route } = useLocation();
  const busca = url.includes('?') ? url.slice(url.indexOf('?')) : '';
  const estado = useMemo(() => lerEstadoDaConsulta(busca), [busca]);
  const [filtros, definirFiltros] = useState<FiltrosDePedidos>(estado.filtros);
  const [resultado, definirResultado] = useState<Resultado | null>(null);
  const [recarga, definirRecarga] = useState(0);
  const [emAndamento, definirEmAndamento] = useState<number | null>(null);
  const [problemaDaAcao, definirProblemaDaAcao] = useState<ProblemaDetalhe | null>(null);
  const contagem = useCarregamento((signal) => cliente.GET('/api/admin/pedidos/contagem', { signal }), [recarga]);

  useEffect(() => {
    definirFiltros(estado.filtros);
  }, [estado]);

  useEffect(() => {
    if (!numeroValido(estado.filtros.numero) || !numeroValido(estado.filtros.clienteId)) {
      const detail = numeroValido(estado.filtros.numero) ? CODIGO_INVALIDO : NUMERO_INVALIDO;
      definirResultado({ busca, pagina: null, problema: { status: 400, detail } });
      return;
    }
    const controle = new AbortController();
    cliente
      .GET('/api/admin/pedidos', {
        params: { query: consultaDoEstado(estado) },
        signal: controle.signal,
      })
      .then(({ data, error, response }) => {
        if (controle.signal.aborted) {
          return;
        }
        definirResultado(
          response.ok
            ? { busca, pagina: data ?? null, problema: null }
            : { busca, pagina: null, problema: interpretarProblema(error, response.status) },
        );
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirResultado({
            busca,
            pagina: null,
            problema: { status: 0, detail: 'Não foi possível carregar os pedidos. Tente novamente.' },
          });
        }
      });
    return () => controle.abort();
  }, [busca, estado, recarga]);

  /** Avança o pedido direto da listagem (RN-VEN-009, RN-VEN-010), como no detalhe. */
  async function avancar(id: number, acao: 'despachar' | 'confirmar-entrega') {
    definirEmAndamento(id);
    definirProblemaDaAcao(null);
    const caminho = { params: { path: { id } } };
    try {
      const { error, response } =
        acao === 'despachar'
          ? await cliente.POST('/api/admin/pedidos/{id}/despachar', caminho)
          : await cliente.POST('/api/admin/pedidos/{id}/confirmar-entrega', caminho);
      if (!response.ok) {
        definirProblemaDaAcao(interpretarProblema(error, response.status));
      }
    } catch {
      definirProblemaDaAcao({ status: 0, detail: 'Não foi possível atualizar o pedido. Tente novamente.' });
    } finally {
      definirEmAndamento(null);
      definirRecarga((atual) => atual + 1);
    }
  }

  function mudarCampo(campo: CampoDeTexto, valor: string) {
    definirFiltros((atual) => ({ ...atual, [campo]: valor }));
  }

  function filtrar(evento: Event) {
    evento.preventDefault();
    route(urlDaConsulta({ filtros, ordem: estado.ordem, pagina: 1 }));
  }

  const { status: _status, ...avancados } = estado.filtros;
  const filtrosAvancados = Object.values(avancados).filter((valor) =>
    Array.isArray(valor) ? valor.length > 0 : valor !== '',
  ).length;
  const visivel = resultado?.busca === busca ? resultado : null;
  const pagina = visivel?.pagina ?? null;

  return (
    <section class="pagina">
      <header>
        <h1>Pedidos</h1>
        <p class="pagina__introducao">
          Consulte os pedidos por qualquer combinação dos campos. O período considera a data da finalização.
        </p>
      </header>

      <AbasDeStatus
        contagem={contagem.dados}
        marcados={estado.filtros.status}
        urlDoStatus={(status) =>
          urlDaConsulta({ ...estado, filtros: { ...estado.filtros, status: status ? [status] : [] }, pagina: 1 })}
      />

      <details class="cartao filtros-recolhidos" open={filtrosAvancados > 0}>
        <summary>Mais filtros{filtrosAvancados > 0 && ` (${filtrosAvancados})`}</summary>
      <form class="filtros" role="search" aria-label="Filtrar pedidos" onSubmit={filtrar}>
        <CampoDeFormulario id="filtro-numero" rotulo="Número do pedido" valor={filtros.numero} name="numero"
          inputMode="numeric" erro={numeroValido(filtros.numero) ? undefined : NUMERO_INVALIDO}
          aoMudar={(valor) => mudarCampo('numero', valor)} />
        <CampoDeFormulario id="filtro-cliente-id" rotulo="Código do cliente" valor={filtros.clienteId}
          name="clienteId" inputMode="numeric"
          erro={numeroValido(filtros.clienteId) ? undefined : CODIGO_INVALIDO}
          aoMudar={(valor) => mudarCampo('clienteId', valor)} />
        <CampoDeFormulario id="filtro-cliente" rotulo="Cliente" valor={filtros.cliente} name="cliente"
          aoMudar={(valor) => mudarCampo('cliente', valor)} />
        <CampoDeFormulario id="filtro-cpf" rotulo="CPF" valor={filtros.cpf} name="cpf" inputMode="numeric"
          aoMudar={(valor) => mudarCampo('cpf', valor)} />
        <CampoDeFormulario id="filtro-realizado-de" rotulo="Realizado de" tipo="date" name="realizadoEmDe"
          valor={filtros.realizadoEmDe} aoMudar={(valor) => mudarCampo('realizadoEmDe', valor)} />
        <CampoDeFormulario id="filtro-realizado-ate" rotulo="Realizado até" tipo="date" name="realizadoEmAte"
          valor={filtros.realizadoEmAte} aoMudar={(valor) => mudarCampo('realizadoEmAte', valor)} />

        <OpcoesMarcaveis legenda="Status" nome="status" opcoes={STATUS} marcados={filtros.status}
          alternar={(valor) =>
            definirFiltros((atual) => ({ ...atual, status: alternarNaOrdem(STATUS, atual.status, valor) }))} />
        <OpcoesMarcaveis legenda="Situação da troca" nome="situacao" opcoes={SITUACOES_DA_TROCA}
          marcados={filtros.situacao}
          alternar={(valor) => definirFiltros((atual) => ({
            ...atual,
            situacao: alternarNaOrdem(SITUACOES_DA_TROCA, atual.situacao, valor),
          }))} />

        <div class="acoes">
          <Botao type="submit" disabled={!numeroValido(filtros.numero) || !numeroValido(filtros.clienteId)}>
            Filtrar
          </Botao>
          <Botao variante="secundario"
            type="button"
            onClick={() => {
              definirFiltros(filtrosVazios());
              route('/admin/pedidos');
            }}
          >
            Limpar filtros
          </Botao>
        </div>
      </form>
      </details>

      <Erro problema={problemaDaAcao} />

      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando pedidos…
        </p>
      ) : visivel.problema ? (
        <Erro problema={visivel.problema} />
      ) : pagina !== null && pagina.itens.length === 0 && pagina.totalItens > 0 ? (
        <p class="aviso">
          Esta página não existe mais. <a href={urlDaConsulta({ ...estado, pagina: 1 })}>Ir para a primeira página</a>
        </p>
      ) : pagina === null || pagina.itens.length === 0 ? (
        <p class="aviso">Nenhum pedido encontrado.</p>
      ) : (
        <>
          <div class="resultado__cabecalho">
            <p>{pagina.totalItens === 1 ? '1 pedido encontrado' : `${pagina.totalItens} pedidos encontrados`}</p>
            <SeletorDeOrdem
              id="ordem-dos-pedidos"
              valor={estado.ordem}
              aoMudar={(ordem) => route(urlDaConsulta({ ...estado, ordem, pagina: 1 }))}
              opcoes={ORDENS}
            />
          </div>

          <div class="tabela-rolavel">
            <table class="tabela">
              <thead>
                <tr>
                  <th scope="col">Número</th>
                  <th scope="col">Realizado em</th>
                  <th scope="col">Cliente</th>
                  <th scope="col">CPF</th>
                  <th scope="col">Status</th>
                  <th scope="col">Total</th>
                  <th scope="col"><span class="visualmente-oculto">Ações</span></th>
                </tr>
              </thead>
              <tbody>
                {pagina.itens.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <a href={`/admin/pedidos/${item.id}`}>{item.id}</a>
                    </td>
                    <td>{formatarDataHora(item.realizadoEm)}</td>
                    <td>{item.clienteNome}</td>
                    <td>{formatarCpf(item.clienteCpf)}</td>
                    <td><EtiquetaDeStatus status={item.status} /></td>
                    <td>{formatarDinheiro(item.total)}</td>
                    <td class="tabela__acoes">
                      {item.status === 'APROVADA' && (
                        <Botao variante="secundario" disabled={emAndamento !== null}
                          onClick={() => void avancar(item.id, 'despachar')}
                          aria-label={`Despachar o pedido ${item.id}`}>
                          Despachar
                        </Botao>
                      )}
                      {item.status === 'EM_TRANSPORTE' && (
                        <Botao variante="secundario" disabled={emAndamento !== null}
                          onClick={() => void avancar(item.id, 'confirmar-entrega')}
                          aria-label={`Confirmar a entrega do pedido ${item.id}`}>
                          Confirmar entrega
                        </Botao>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <Paginacao pagina={estado.pagina} totalPaginas={pagina.totalPaginas} rotulo="Páginas de pedidos" urlDaPagina={(numero) => urlDaConsulta({ ...estado, pagina: numero })} />
        </>
      )}
    </section>
  );
}
