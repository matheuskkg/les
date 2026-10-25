import { useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { itensDaSolicitacao } from '../regras/solicitacaoDeTroca';
import { Erro } from '../../../ui/Erro';
import { Violacoes } from '../../../ui/Violacoes';
import { Botao } from '../../../ui/Botao';

type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];

/**
 * Formulário em que o cliente escolhe os itens e quantas unidades de cada um quer trocar (RF-VEN-011, DEC-12). A
 * quantidade vai até a disponível do item; o server confere prazo e quantidades de novo. Depois de enviar, quem
 * usa o formulário avisa o cliente e recarrega o pedido.
 */
export function SolicitarTroca({ pedido, aoSolicitar }: { pedido: PedidoDetalhe; aoSolicitar: () => void }) {
  const [quantidades, definirQuantidades] = useState<Record<number, string>>({});
  const [enviando, definirEnviando] = useState(false);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);

  async function enviar(evento: Event) {
    evento.preventDefault();
    const itens = itensDaSolicitacao(quantidades);
    if (itens.length === 0) {
      definirProblema({ status: 0, detail: 'Informe a quantidade de pelo menos um item para trocar.' });
      return;
    }
    definirEnviando(true);
    definirProblema(null);
    try {
      const { error, response } = await cliente.POST('/api/me/pedidos/{id}/trocas', {
        params: { path: { id: pedido.id } },
        body: { itens },
      });
      if (response.ok) {
        definirQuantidades({});
        aoSolicitar();
      } else {
        definirProblema(interpretarProblema(error, response.status));
      }
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível solicitar a troca. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <form class="cartao" onSubmit={(evento) => void enviar(evento)} noValidate>
      <h2>Solicitar troca</h2>
      <p>Escolha quantas unidades de cada item você quer trocar.</p>
      {pedido.itens
        .filter((item) => item.disponivelParaTroca > 0)
        .map((item) => (
          <div class="campo" key={item.id}>
            <label class="campo__rotulo" for={`troca-${item.id}`}>
              {item.livro.titulo} (até {item.disponivelParaTroca})
            </label>
            <input
              class="campo__entrada"
              id={`troca-${item.id}`}
              type="number"
              inputMode="numeric"
              min={0}
              max={item.disponivelParaTroca}
              step={1}
              value={quantidades[item.id] ?? ''}
              onInput={(evento) => {
                const valor = evento.currentTarget.value;
                definirQuantidades((atuais) => ({ ...atuais, [item.id]: valor }));
              }}
            />
          </div>
        ))}
      {problema?.violacoes && problema.violacoes.length > 0 ? (
        <Violacoes violacoes={problema.violacoes} />
      ) : (
        <Erro problema={problema} />
      )}
      <div class="acoes">
        <Botao type="submit" disabled={enviando}>
          Solicitar troca
        </Botao>
      </div>
    </form>
  );
}
