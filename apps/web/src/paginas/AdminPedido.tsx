import { useEffect, useRef, useState } from 'preact/hooks';
import { useRoute } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { CopiasDoPedido } from '../componentes/CopiasDoPedido';
import { AcoesDaTroca } from '../componentes/AcoesDaTroca';
import { Erro } from '../componentes/Erro';
import { TrocasDoPedido } from '../componentes/TrocasDoPedido';
import { formatarDataHora, textoDoStatus } from '../formatacao';

type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];

interface Resultado {
  id: string;
  pedido: PedidoDetalhe | null;
  problema: ProblemaDetalhe | null;
}

type Acao = 'despachar' | 'confirmar-entrega';

/** Ação do admin disponível em cada status do diagrama do DEC-12 (RN-VEN-009, RN-VEN-010). */
const ACOES: Partial<Record<PedidoDetalhe['status'], { acao: Acao; rotulo: string }>> = {
  APROVADA: { acao: 'despachar', rotulo: 'Despachar' },
  EM_TRANSPORTE: { acao: 'confirmar-entrega', rotulo: 'Confirmar entrega' },
};

/**
 * Detalhe do pedido para o administrador com as cópias gravadas na finalização: cliente, endereços, itens,
 * cartões, cupons e valores (RF-VEN-009, RF-VEN-010, DEC-12). Daqui o admin despacha o pedido aprovado e
 * confirma a entrega do pedido em transporte, e autoriza ou recusa as trocas `EM TROCA` (RF-VEN-012).
 */
export default function AdminPedido() {
  const { params } = useRoute();
  const id = params.id ?? '';
  const [resultado, definirResultado] = useState<Resultado | null>(null);
  // Ação em andamento e erro dela ficam presos ao pedido da tela; trocar de pedido não os herda.
  const [enviandoEm, definirEnviandoEm] = useState<string | null>(null);
  const [problemaDaAcao, definirProblemaDaAcao] = useState<{ id: string; problema: ProblemaDetalhe } | null>(null);
  const [recarga, definirRecarga] = useState(0);
  const idDaTela = useRef(id);
  idDaTela.current = id;

  useEffect(() => {
    const numero = Number(id);
    if (!/^\d+$/.test(id) || !Number.isSafeInteger(numero)) {
      definirResultado({ id, pedido: null, problema: { status: 404, detail: 'Pedido não encontrado.' } });
      return;
    }
    const controle = new AbortController();
    cliente
      .GET('/api/admin/pedidos/{id}', { params: { path: { id: numero } }, signal: controle.signal })
      .then(({ data, error, response }) => {
        if (!controle.signal.aborted) {
          definirResultado(
            response.ok
              ? { id, pedido: data ?? null, problema: null }
              : { id, pedido: null, problema: interpretarProblema(error, response.status) },
          );
        }
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirResultado({
            id,
            pedido: null,
            problema: { status: 0, detail: 'Não foi possível carregar o pedido. Tente novamente.' },
          });
        }
      });
    return () => controle.abort();
  }, [id, recarga]);

  const visivel = resultado?.id === id ? resultado : null;
  const pedido = visivel?.pedido ?? null;
  const acao = pedido === null ? undefined : ACOES[pedido.status];

  /**
   * Despacha ou confirma a entrega. Sem a resposta do pedido (409 porque outra aba já mudou o status, ou corpo
   * vazio), o pedido é recarregado para a tela mostrar o status atual e a ação certa.
   */
  async function executar(atual: PedidoDetalhe, escolhida: Acao) {
    const daTela = id;
    definirEnviandoEm(daTela);
    definirProblemaDaAcao(null);
    try {
      const caminho = { params: { path: { id: atual.id } } };
      const { data, error, response } =
        escolhida === 'despachar'
          ? await cliente.POST('/api/admin/pedidos/{id}/despachar', caminho)
          : await cliente.POST('/api/admin/pedidos/{id}/confirmar-entrega', caminho);
      if (idDaTela.current !== daTela) {
        return;
      }
      if (response.ok && data) {
        definirResultado({ id: daTela, pedido: data, problema: null });
        return;
      }
      if (!response.ok) {
        definirProblemaDaAcao({ id: daTela, problema: interpretarProblema(error, response.status) });
      }
      definirRecarga((atualizacao) => atualizacao + 1);
    } catch {
      if (idDaTela.current === daTela) {
        definirProblemaDaAcao({
          id: daTela,
          problema: { status: 0, detail: 'Não foi possível alterar o pedido. Tente novamente.' },
        });
      }
    } finally {
      definirEnviandoEm((emCurso) => (emCurso === daTela ? null : emCurso));
    }
  }

  /** Resposta da ação numa troca; ignorada se o admin já foi para outro pedido. Sem pedido, recarrega a tela. */
  function aoAlterarTroca(daTela: string, alterado: PedidoDetalhe | null) {
    if (idDaTela.current !== daTela) {
      return;
    }
    if (alterado) {
      definirResultado({ id: daTela, pedido: alterado, problema: null });
    } else {
      definirRecarga((atual) => atual + 1);
    }
  }

  return (
    <section class="pagina">
      <p>
        <a href="/admin/pedidos">← Voltar para os pedidos</a>
      </p>

      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : pedido === null ? (
        <>
          <h1>Pedido não encontrado</h1>
          <Erro problema={visivel.problema} />
        </>
      ) : (
        <>
          <h1>Pedido {pedido.id}</h1>
          <div class="cartao">
            <h2>Situação</h2>
            <dl class="detalhe__campos">
              <div><dt>Status</dt><dd data-testid="status-do-pedido">{textoDoStatus(pedido.status)}</dd></div>
              <div><dt>Realizado em</dt><dd>{formatarDataHora(pedido.realizadoEm)}</dd></div>
              {pedido.entregueEm && (
                <div><dt>Entregue em</dt><dd>{formatarDataHora(pedido.entregueEm)}</dd></div>
              )}
            </dl>
            {acao && (
              <div class="acoes">
                <button class="botao" type="button" disabled={enviandoEm === id} onClick={() => void executar(pedido, acao.acao)}>
                  {acao.rotulo}
                </button>
              </div>
            )}
            {problemaDaAcao?.id === id && <Erro problema={problemaDaAcao.problema} />}
          </div>

          <TrocasDoPedido
            pedido={pedido}
            acoes={(troca) =>
              troca.status === 'EM_TROCA' ? (
                <AcoesDaTroca key={`${pedido.id}-${troca.id}`} troca={troca} aoAlterar={(alterado) => aoAlterarTroca(id, alterado)} />
              ) : null}
          />
          <CopiasDoPedido pedido={pedido} linkDoCliente={`/admin/clientes/${pedido.cliente.id}`} />
        </>
      )}
    </section>
  );
}
