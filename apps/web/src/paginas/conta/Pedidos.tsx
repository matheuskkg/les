import { useEffect, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../api/cliente';
import type { components } from '../../api/esquema';
import { CopiasDoPedido } from '../../componentes/CopiasDoPedido';
import { Erro } from '../../componentes/Erro';
import { useRecargaPorNotificacao } from '../../contextoDeNotificacoes';
import { SolicitarTroca } from '../../componentes/SolicitarTroca';
import { TrocasDoPedido } from '../../componentes/TrocasDoPedido';
import { formatarDataHora, formatarDinheiro, textoDoStatus } from '../../formatacao';

type PaginaDePedidos = components['schemas']['PaginaPedidoListadoDto'];
type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];

const TAMANHO_DA_PAGINA = 20;

interface Lista {
  pagina: number;
  dados: PaginaDePedidos | null;
  problema: ProblemaDetalhe | null;
}

function urlDaPagina(pagina: number): string {
  return pagina > 1 ? `/minha-conta/pedidos?pagina=${pagina}` : '/minha-conta/pedidos';
}

/**
 * Todas as transações do cliente logado, das mais recentes às mais antigas, inclusive as reprovadas (RF-CLI-005). A
 * página fica na URL, para o "Voltar" do detalhe e os links voltarem a ela.
 */
export function MeusPedidos() {
  const { query, route } = useLocation();
  const lida = Number.parseInt(query.pagina ?? '', 10);
  const pagina = Number.isSafeInteger(lida) && lida > 1 ? lida : 1;
  const [lista, definirLista] = useState<Lista | null>(null);

  useEffect(() => {
    const controle = new AbortController();
    cliente
      .GET('/api/me/pedidos', {
        params: { query: { pagina: pagina - 1, tamanho: TAMANHO_DA_PAGINA, ordem: ['realizadoEm,desc'] } },
        signal: controle.signal,
      })
      .then(({ data, error, response }) => {
        if (!controle.signal.aborted) {
          definirLista(
            response.ok
              ? { pagina, dados: data ?? null, problema: null }
              : { pagina, dados: null, problema: interpretarProblema(error, response.status) },
          );
        }
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirLista({
            pagina,
            dados: null,
            problema: { status: 0, detail: 'Não foi possível carregar os pedidos. Tente novamente.' },
          });
        }
      });
    return () => controle.abort();
  }, [pagina]);

  const visivel = lista?.pagina === pagina ? lista : null;
  const dados = visivel?.dados ?? null;

  return (
    <div class="cartao">
      <h2>Meus pedidos</h2>
      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando pedidos…
        </p>
      ) : visivel.problema ? (
        <Erro problema={visivel.problema} />
      ) : dados !== null && dados.itens.length === 0 && dados.totalItens > 0 ? (
        <p class="aviso">
          Esta página não existe mais. <a href={urlDaPagina(1)}>Ir para a primeira página</a>
        </p>
      ) : dados === null || dados.totalItens === 0 ? (
        <p>Você ainda não fez nenhum pedido.</p>
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
                      <a href={pagina > 1 ? `/minha-conta/pedidos/${item.id}?de=${pagina}` : `/minha-conta/pedidos/${item.id}`}>
                        {item.id}
                      </a>
                    </td>
                    <td>{formatarDataHora(item.realizadoEm)}</td>
                    <td>{textoDoStatus(item.status)}</td>
                    <td>{formatarDinheiro(item.total)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {dados.totalPaginas > 1 && (
            <nav class="paginacao" aria-label="Páginas de pedidos">
              <button class="botao botao--secundario" type="button" disabled={pagina <= 1}
                onClick={() => route(urlDaPagina(pagina - 1))}>
                Anterior
              </button>
              <span class="paginacao__posicao">
                Página {pagina} de {dados.totalPaginas}
              </span>
              <button class="botao botao--secundario" type="button" disabled={pagina >= dados.totalPaginas}
                onClick={() => route(urlDaPagina(pagina + 1))}>
                Próxima
              </button>
            </nav>
          )}
        </>
      )}
    </div>
  );
}

interface Detalhe {
  id: string;
  pedido: PedidoDetalhe | null;
  problema: ProblemaDetalhe | null;
}

/**
 * Detalhe de um pedido do cliente logado, com as cópias gravadas na finalização e as trocas (RF-CLI-005, DEC-12). Do
 * pedido entregue, dentro do prazo, o cliente pede a troca de itens (RF-VEN-011).
 */
export function MeuPedido({ id }: { id: string }) {
  const { query } = useLocation();
  const voltar = query.de === undefined ? '/minha-conta/pedidos' : urlDaPagina(Number.parseInt(query.de, 10) || 1);
  const [detalhe, definirDetalhe] = useState<Detalhe | null>(null);
  const [recarga, definirRecarga] = useState(0);
  // Aviso da troca recém-pedida, preso ao pedido: sobrevive ao recarregamento, que pode esconder o formulário.
  const [trocaSolicitadaEm, definirTrocaSolicitadaEm] = useState<string | null>(null);

  // Autorização e recusa da troca pelo admin chegam por notificação e atualizam o detalhe (DEC-13).
  useRecargaPorNotificacao('troca', () => definirRecarga((atual) => atual + 1));

  useEffect(() => {
    const numero = Number(id);
    if (!/^\d+$/.test(id) || !Number.isSafeInteger(numero)) {
      definirDetalhe({ id, pedido: null, problema: { status: 404, detail: 'Pedido não encontrado.' } });
      return;
    }
    const controle = new AbortController();
    cliente
      .GET('/api/me/pedidos/{id}', { params: { path: { id: numero } }, signal: controle.signal })
      .then(({ data, error, response }) => {
        if (!controle.signal.aborted) {
          definirDetalhe(
            response.ok
              ? { id, pedido: data ?? null, problema: null }
              : { id, pedido: null, problema: interpretarProblema(error, response.status) },
          );
        }
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirDetalhe({
            id,
            pedido: null,
            problema: { status: 0, detail: 'Não foi possível carregar o pedido. Tente novamente.' },
          });
        }
      });
    return () => controle.abort();
  }, [id, recarga]);

  const visivel = detalhe?.id === id ? detalhe : null;
  const pedido = visivel?.pedido ?? null;

  return (
    <>
      <p>
        <a href={voltar}>← Voltar para os meus pedidos</a>
      </p>
      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : pedido === null ? (
        <div class="cartao">
          <h2>{visivel.problema?.status === 404 ? 'Pedido não encontrado' : 'Não foi possível abrir o pedido'}</h2>
          <Erro problema={visivel.problema} />
        </div>
      ) : (
        <>
          <div class="cartao">
            <h2>Pedido {pedido.id}</h2>
            <dl class="detalhe__campos">
              <div><dt>Status</dt><dd data-testid="status-do-pedido">{textoDoStatus(pedido.status)}</dd></div>
              <div><dt>Realizado em</dt><dd>{formatarDataHora(pedido.realizadoEm)}</dd></div>
              {pedido.entregueEm && (
                <div><dt>Entregue em</dt><dd>{formatarDataHora(pedido.entregueEm)}</dd></div>
              )}
            </dl>
          </div>
          {trocaSolicitadaEm === id && (
            <p class="aviso" role="status">
              Troca solicitada. Avisaremos quando ela for analisada.
            </p>
          )}
          <TrocasDoPedido pedido={pedido} />
          {pedido.podeSolicitarTroca && (
            <SolicitarTroca
              key={pedido.id}
              pedido={pedido}
              aoSolicitar={() => {
                definirTrocaSolicitadaEm(id);
                definirRecarga((atual) => atual + 1);
              }}
            />
          )}
          <CopiasDoPedido pedido={pedido} />
        </>
      )}
    </>
  );
}
