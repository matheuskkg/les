import { useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../../api/cliente';
import { CampoDeFormulario } from '../../../../ui/CampoDeFormulario';
import { Erro } from '../../../../ui/Erro';
import { Violacoes } from '../../../../ui/Violacoes';
import { Botao } from '../../../../ui/Botao';

interface Formulario {
  senhaAtual: string;
  novaSenha: string;
  confirmacaoSenha: string;
}

const VAZIO: Formulario = { senhaAtual: '', novaSenha: '', confirmacaoSenha: '' };

/** Troca de senha do usuário logado; a sessão atual continua válida (RF-CLI-008, DEC-08). */
export function Senha() {
  const [formulario, definirFormulario] = useState<Formulario>(VAZIO);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [alterada, definirAlterada] = useState(false);
  const [enviando, definirEnviando] = useState(false);

  function mudar(campo: keyof Formulario, valor: string) {
    definirAlterada(false);
    definirFormulario((atual) => ({ ...atual, [campo]: valor }));
  }

  async function enviar(evento: Event) {
    evento.preventDefault();
    if (enviando) {
      return;
    }
    definirEnviando(true);
    definirProblema(null);
    try {
      const { error, response } = await cliente.POST('/api/me/senha', { body: formulario });
      if (!response.ok) {
        definirProblema(interpretarProblema(error, response.status));
        return;
      }
      definirFormulario(VAZIO);
      definirAlterada(true);
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível conectar ao servidor. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <form class="formulario cartao" noValidate onSubmit={(evento) => void enviar(evento)}>
      <h2>Alterar senha</h2>
      {problema?.violacoes && problema.violacoes.length > 0 ? (
        <Violacoes violacoes={problema.violacoes} />
      ) : (
        <Erro problema={problema} />
      )}
      {alterada && (
        <p class="aviso" role="status">
          Senha alterada.
        </p>
      )}
      <CampoDeFormulario id="senha-atual" rotulo="Senha atual" tipo="password" required
        autoComplete="current-password" valor={formulario.senhaAtual}
        aoMudar={(valor) => mudar('senhaAtual', valor)} />
      <CampoDeFormulario id="senha-nova" rotulo="Nova senha" tipo="password" required
        autoComplete="new-password" valor={formulario.novaSenha}
        ajuda="Pelo menos 8 caracteres, com letras maiúsculas e minúsculas e um caractere especial."
        aoMudar={(valor) => mudar('novaSenha', valor)} />
      <CampoDeFormulario id="senha-confirmacao" rotulo="Confirme a nova senha" tipo="password" required
        autoComplete="new-password" valor={formulario.confirmacaoSenha}
        aoMudar={(valor) => mudar('confirmacaoSenha', valor)} />
      <div class="acoes">
        <Botao type="submit" disabled={enviando}>
          {enviando ? 'Salvando…' : 'Alterar senha'}
        </Botao>
      </div>
    </form>
  );
}
