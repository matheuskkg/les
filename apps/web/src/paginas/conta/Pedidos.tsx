import { useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../api/cliente';
import type { components } from '../../api/esquema';
import { CopiasDoPedido } from '../../componentes/CopiasDoPedido';
import { Erro } from '../../componentes/Erro';
import { formatarDataHora, formatarDinheiro, textoDoStatus } from '../../formatacao';

type PaginaDePedidos = components['schemas']['PaginaPedidoListadoDto'];
type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];

const TAMANHO_DA_PAGINA = 20;

interface Lista {
  pagina: number;
  dados: PaginaDePedidos | null;
  problema: ProblemaDetalhe | null;
}

/** Todas as transações do cliente logado, das mais recentes às mais antigas, inclusive as reprovadas (RF-CLI-005). */
export function MeusPedidos() {
  const [pagina, definirPagina] = useState(1);
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
                      <a href={`/minha-conta/pedidos/${item.id}`}>{item.id}</a>
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
                onClick={() => definirPagina(pagina - 1)}>
                Anterior
              </button>
              <span class="paginacao__posicao">
                Página {pagina} de {dados.totalPaginas}
              </span>
              <button class="botao botao--secundario" type="button" disabled={pagina >= dados.totalPaginas}
                onClick={() => definirPagina(pagina + 1)}>
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

/** Detalhe de um pedido do cliente logado, com as cópias gravadas na finalização (RF-CLI-005, DEC-12). */
export function MeuPedido({ id }: { id: string }) {
  const [detalhe, definirDetalhe] = useState<Detalhe | null>(null);

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
  }, [id]);

  const visivel = detalhe?.id === id ? detalhe : null;
  const pedido = visivel?.pedido ?? null;

  return (
    <>
      <p>
        <a href="/minha-conta/pedidos">← Voltar para os meus pedidos</a>
      </p>
      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : pedido === null ? (
        <div class="cartao">
          <h2>Pedido não encontrado</h2>
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
          <CopiasDoPedido pedido={pedido} />
        </>
      )}
    </>
  );
}
