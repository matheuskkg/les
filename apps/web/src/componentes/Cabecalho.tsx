import { useSessao } from '../contextoDeSessao';

export function Cabecalho() {
  const { usuario, carregando, encerrarSessao } = useSessao();

  return (
    <header class="cabecalho">
      <a class="marca" href="/">
        Livraria
      </a>

      <nav class="navegacao" aria-label="Principal">
        <a href="/">Vitrine</a>
        {usuario?.papel === 'CLIENTE' && <a href="/minha-conta">Minha conta</a>}
        {usuario?.papel === 'ADMIN' && <a href="/admin">Administração</a>}
      </nav>

      <div class="sessao">
        {carregando ? (
          <span class="sessao__carregando" role="status">
            Carregando…
          </span>
        ) : usuario ? (
          <>
            <span class="sessao__usuario" title={usuario.email}>
              {usuario.email}
            </span>
            <button
              type="button"
              class="botao botao--secundario"
              onClick={() => {
                void encerrarSessao();
              }}
            >
              Sair
            </button>
          </>
        ) : (
          <>
            <a class="botao botao--secundario" href="/login">
              Entrar
            </a>
            <a class="botao" href="/cadastro">
              Criar conta
            </a>
          </>
        )}
      </div>
    </header>
  );
}
