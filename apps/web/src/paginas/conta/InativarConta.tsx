import { useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../api/cliente';
import { Erro } from '../../componentes/Erro';
import { useSessao } from '../../contextoDeSessao';

/**
 * Inativação da própria conta (RF-CLI-003, DEC-10). Pede uma segunda confirmação na
 * própria página, encerra a sessão e lembra que entrar em até 7 dias reativa a conta.
 */
export function InativarConta() {
  const { definirUsuario } = useSessao();
  const { route } = useLocation();
  const [confirmando, definirConfirmando] = useState(false);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [enviando, definirEnviando] = useState(false);

  async function inativar() {
    definirEnviando(true);
    definirProblema(null);
    try {
      const { error, response } = await cliente.POST('/api/me/inativar');
      if (!response.ok) {
        definirProblema(interpretarProblema(error, response.status));
        return;
      }
      definirUsuario(null);
      route('/', true);
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível conectar ao servidor. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <section class="cartao zona-de-perigo" aria-labelledby="titulo-inativar">
      <h2 id="titulo-inativar">Inativar conta</h2>
      <p>
        A conta fica inativa e a sessão é encerrada. Se você entrar de novo em até 7 dias, ela é
        reativada; depois disso, fica encerrada. Pedidos em andamento continuam normalmente.
      </p>
      <Erro problema={problema} />
      {confirmando ? (
        <div class="acoes">
          <button class="botao botao--perigo" type="button" disabled={enviando} onClick={() => void inativar()}>
            {enviando ? 'Inativando…' : 'Confirmar inativação'}
          </button>
          <button class="botao botao--secundario" type="button" onClick={() => definirConfirmando(false)}>
            Cancelar
          </button>
        </div>
      ) : (
        <div class="acoes">
          <button class="botao botao--secundario" type="button" onClick={() => definirConfirmando(true)}>
            Inativar minha conta
          </button>
        </div>
      )}
    </section>
  );
}
