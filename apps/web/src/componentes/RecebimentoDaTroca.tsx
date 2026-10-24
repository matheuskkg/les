import { useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { Erro } from './Erro';
import { Violacoes } from './Violacoes';

type Troca = components['schemas']['TrocaDto'];

interface Props {
  troca: Troca;
  tituloDoItem: (itemPedidoId: number) => string;
  /** Chamado depois da confirmação para a tela recarregar o pedido, com o erro quando a troca já tinha mudado (409). */
  aoAlterar: (conflito: ProblemaDetalhe | null) => void;
}

/**
 * Confirmação do recebimento de uma troca autorizada (RF-VEN-014, RF-VEN-015): o admin marca os itens que voltam ao
 * estoque, todos marcados de início. A troca passa a `TROCADO` e gera o cupom de troca do cliente, também pelos itens
 * que não voltam.
 */
export function RecebimentoDaTroca({ troca, tituloDoItem, aoAlterar }: Props) {
  const [voltaEstoque, definirVoltaEstoque] = useState<Record<number, boolean>>(() =>
    Object.fromEntries(troca.itens.map((item) => [item.itemPedidoId, true])),
  );
  const [enviando, definirEnviando] = useState(false);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);

  async function confirmar(evento: Event) {
    evento.preventDefault();
    definirEnviando(true);
    definirProblema(null);
    try {
      const { error, response } = await cliente.POST('/api/admin/trocas/{id}/confirmar-recebimento', {
        params: { path: { id: troca.id } },
        body: {
          itens: troca.itens.map((item) => ({
            itemPedidoId: item.itemPedidoId,
            voltaEstoque: voltaEstoque[item.itemPedidoId] === true,
          })),
        },
      });
      if (response.ok) {
        aoAlterar(null);
        return;
      }
      const problemaDaResposta = interpretarProblema(error, response.status);
      if (response.status === 409) {
        aoAlterar(problemaDaResposta);
        return;
      }
      definirProblema(problemaDaResposta);
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível confirmar o recebimento. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <form onSubmit={(evento) => void confirmar(evento)} noValidate>
      <fieldset class="grupo-de-categorias">
        <legend>Voltam ao estoque</legend>
        <div class="categorias">
          {troca.itens.map((item) => (
            <label class="categoria-opcao" key={item.itemPedidoId}>
              <input
                type="checkbox"
                checked={voltaEstoque[item.itemPedidoId] === true}
                onChange={(evento) => {
                  const marcado = evento.currentTarget.checked;
                  definirVoltaEstoque((atual) => ({ ...atual, [item.itemPedidoId]: marcado }));
                }}
              />
              {tituloDoItem(item.itemPedidoId)}
            </label>
          ))}
        </div>
      </fieldset>
      <div class="acoes">
        <button class="botao" type="submit" disabled={enviando}>
          Confirmar recebimento
        </button>
      </div>
      {problema?.violacoes && problema.violacoes.length > 0 ? (
        <Violacoes violacoes={problema.violacoes} />
      ) : (
        <Erro problema={problema} />
      )}
    </form>
  );
}
