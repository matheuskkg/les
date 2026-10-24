import { useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { Erro } from './Erro';
import { Violacoes } from './Violacoes';

type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];
type Troca = components['schemas']['TrocaDto'];

/** Limite do motivo, o mesmo da regra do server (RF-VEN-012). */
const TAMANHO_MAXIMO_DO_MOTIVO = 300;

interface Props {
  troca: Troca;
  /** Pedido devolvido pela ação, ou nulo quando é preciso recarregá-lo (409 ou resposta sem corpo). */
  aoAlterar: (pedido: PedidoDetalhe | null) => void;
}

/**
 * Ações do admin numa troca `EM TROCA`: autorizar (RF-VEN-012) ou recusar com motivo obrigatório (DEC-12). O
 * estado é de cada troca; a tela usa a chave da troca para não herdá-lo de outra.
 */
export function AcoesDaTroca({ troca, aoAlterar }: Props) {
  const [enviando, definirEnviando] = useState(false);
  const [recusando, definirRecusando] = useState(false);
  const [motivo, definirMotivo] = useState('');
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);

  async function executar(acao: 'autorizar' | 'recusar') {
    if (acao === 'recusar' && motivo.trim() === '') {
      definirProblema({ status: 0, detail: 'Informe o motivo da recusa da troca.' });
      return;
    }
    definirEnviando(true);
    definirProblema(null);
    try {
      const caminho = { params: { path: { id: troca.id } } };
      const { data, error, response } =
        acao === 'autorizar'
          ? await cliente.POST('/api/admin/trocas/{id}/autorizar', caminho)
          : await cliente.POST('/api/admin/trocas/{id}/recusar', { ...caminho, body: { motivo } });
      if (response.ok) {
        aoAlterar(data ?? null);
        return;
      }
      definirProblema(interpretarProblema(error, response.status));
      if (response.status === 409) {
        aoAlterar(null);
      }
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível alterar a troca. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <div class="acoes-da-troca">
      {recusando ? (
        <form
          onSubmit={(evento) => {
            evento.preventDefault();
            void executar('recusar');
          }}
          noValidate
        >
          <div class="campo">
            <label class="campo__rotulo" for={`motivo-${troca.id}`}>
              Motivo da recusa
            </label>
            <textarea
              class="campo__entrada"
              id={`motivo-${troca.id}`}
              rows={3}
              maxLength={TAMANHO_MAXIMO_DO_MOTIVO}
              required
              value={motivo}
              onInput={(evento) => definirMotivo(evento.currentTarget.value)}
            />
            <p class="campo__ajuda">Vai para o cliente na notificação. Até {TAMANHO_MAXIMO_DO_MOTIVO} caracteres.</p>
          </div>
          <div class="acoes">
            <button class="botao" type="submit" disabled={enviando}>
              Confirmar recusa
            </button>
            <button class="botao botao--secundario" type="button" disabled={enviando}
              onClick={() => {
                definirRecusando(false);
                definirProblema(null);
              }}>
              Cancelar
            </button>
          </div>
        </form>
      ) : (
        <div class="acoes">
          <button class="botao" type="button" disabled={enviando} onClick={() => void executar('autorizar')}>
            Autorizar
          </button>
          <button class="botao botao--secundario" type="button" disabled={enviando}
            onClick={() => definirRecusando(true)}>
            Recusar
          </button>
        </div>
      )}
      {problema?.violacoes && problema.violacoes.length > 0 ? (
        <Violacoes violacoes={problema.violacoes} />
      ) : (
        <Erro problema={problema} />
      )}
    </div>
  );
}
