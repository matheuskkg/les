import { useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { ErroDaApi, type ProblemaDetalhe } from '../../../api/cliente';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { Erro } from '../../../ui/Erro';
import { Violacoes } from '../../../ui/Violacoes';
import { useSessao } from '../contextoDeSessao';
import { destinoSeguro } from '../regras/destino';
import { entrar } from '../api';
import { Botao } from '../../../ui/Botao';

export default function Login() {
  const { definirUsuario } = useSessao();
  const { query, route } = useLocation();
  const [login, definirLogin] = useState('');
  const [senha, definirSenha] = useState('');
  const [enviando, definirEnviando] = useState(false);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [falha, definirFalha] = useState<string | null>(null);

  async function aoEnviar(evento: Event): Promise<void> {
    evento.preventDefault();
    definirProblema(null);
    definirFalha(null);

    const identificacao = login.trim();
    if (identificacao === '' || senha === '') {
      definirFalha('Informe o login e a senha.');
      return;
    }

    definirEnviando(true);
    try {
      const usuario = await entrar(identificacao, senha);
      definirUsuario(usuario);
      route(destinoSeguro(query.destino, usuario, location.origin), true);
    } catch (erro) {
      if (erro instanceof ErroDaApi) {
        definirProblema(erro.problema);
      } else {
        definirFalha('Não foi possível conectar ao servidor. Tente novamente.');
      }
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <section class="pagina pagina--estreita">
      <h1>Entrar</h1>
      <p class="pagina__introducao">
        Use o e-mail ou telefone cadastrado e sua senha para acessar sua conta.
      </p>

      <form
        class="cartao formulario"
        onSubmit={(evento) => {
          void aoEnviar(evento);
        }}
      >
        <CampoDeFormulario
          id="login"
          rotulo="E-mail ou telefone"
          tipo="text"
          valor={login}
          aoMudar={definirLogin}
          name="login"
          autoComplete="username"
          required
          ajuda="Você pode entrar com o e-mail ou o telefone do cadastro."
        />
        <CampoDeFormulario
          id="senha"
          rotulo="Senha"
          tipo="password"
          valor={senha}
          aoMudar={definirSenha}
          name="senha"
          autoComplete="current-password"
          required
        />

        <Erro problema={problema} mensagem={falha} />
        <Violacoes violacoes={problema?.violacoes} />

        <div class="acoes">
          <Botao type="submit" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </Botao>
        </div>
      </form>

      <p class="pagina__rodape">
        Ainda não tem conta? <a href="/cadastro">Criar conta</a>.
      </p>
    </section>
  );
}
