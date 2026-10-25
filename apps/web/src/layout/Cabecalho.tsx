import { useEffect, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { iniciais } from '../compartilhado/iniciais';
import { PrazoDaReserva, useCarrinho } from '../modulos/carrinho';
import { useNotificacoes } from '../modulos/notificacoes';
import { paginaDeNotificacoes, textoDoContador } from '../modulos/notificacoes/regras/notificacoes';
import { useSessao } from '../modulos/sessao';
import { Botao, BotaoLink } from '../ui/Botao';

/** Busca livre da vitrine (título, autor, editora ou ISBN); acompanha o `q` da URL. */
function BuscaGlobal() {
  const { path, query, route } = useLocation();
  const [termo, definirTermo] = useState(query.q ?? '');

  useEffect(() => {
    if (path === '/') {
      definirTermo(query.q ?? '');
    }
  }, [path, query.q]);

  function buscar(evento: Event) {
    evento.preventDefault();
    const limpo = termo.trim();
    route(limpo === '' ? '/' : `/?q=${encodeURIComponent(limpo)}`);
  }

  return (
    <form class="busca-global" role="search" onSubmit={buscar}>
      <label class="visualmente-oculto" for="busca-global">
        Buscar livros
      </label>
      <input
        id="busca-global"
        type="search"
        placeholder="Título, autor, editora ou ISBN"
        value={termo}
        onInput={(evento) => definirTermo(evento.currentTarget.value)}
      />
      <button type="submit">Buscar</button>
    </form>
  );
}

function BotaoDoCarrinho() {
  const { carrinho, abrirGaveta } = useCarrinho();
  const quantidade = carrinho?.quantidadeDeItens ?? 0;
  return (
    <button
      type="button"
      class="navegacao__link botao-do-carrinho"
      aria-haspopup="dialog"
      aria-label={quantidade > 0 ? `Carrinho: ${quantidade} ${quantidade === 1 ? 'item' : 'itens'}` : 'Carrinho'}
      onClick={abrirGaveta}
    >
      Carrinho
      {quantidade > 0 && <span class="contador contador--neutro">{quantidade}</span>}
    </button>
  );
}

/** Faixa com a contagem regressiva da reserva enquanto há itens no carrinho (RNF-VEN-001). */
function FaixaDeReserva() {
  const { carrinho } = useCarrinho();
  if (!carrinho?.expiraEm || carrinho.itens.length === 0) {
    return null;
  }
  return (
    <div class="faixa-de-reserva">
      <div class="faixa-de-reserva__conteudo">
        <PrazoDaReserva expiraEm={carrinho.expiraEm} prefixo="Seus livros estão reservados por" />
        <a href="/carrinho">Ver carrinho</a>
      </div>
    </div>
  );
}

export function Cabecalho() {
  const { usuario, carregando, encerrarSessao } = useSessao();
  const { naoLidas } = useNotificacoes();
  const contador = textoDoContador(naoLidas);

  return (
    <header class="cabecalho">
      <div class="cabecalho__faixa">
        <a class="marca" href="/">
          Livraria
        </a>

        {usuario?.papel !== 'ADMIN' && <BuscaGlobal />}

        <nav class="navegacao" aria-label="Principal">
          <a class="navegacao__link" href="/">
            Vitrine
          </a>
          {usuario?.papel === 'CLIENTE' && (
            <>
              <BotaoDoCarrinho />
              <a class="navegacao__link" href="/minha-conta">
                Minha conta
              </a>
            </>
          )}
          {usuario?.papel === 'ADMIN' && (
            <a class="navegacao__link" href="/admin">
              Administração
            </a>
          )}
          {usuario && (
            <a
              class="navegacao__link"
              href={paginaDeNotificacoes(usuario.papel)}
              aria-label={naoLidas > 0 ? `Notificações: ${naoLidas} não lidas` : 'Notificações'}
            >
              Notificações
              {contador && <span class="contador">{contador}</span>}
            </a>
          )}
        </nav>

        <div class="sessao">
          {carregando ? (
            <span class="sessao__carregando" role="status">
              Carregando…
            </span>
          ) : usuario ? (
            <>
              <span class="sessao__usuario" title={usuario.email}>
                <span class="avatar" aria-hidden="true">
                  {iniciais(usuario.nome)}
                </span>
                <span class="sessao__nome">{usuario.nome}</span>
              </span>
              <Botao
                variante="secundario"
                onClick={() => {
                  void encerrarSessao();
                }}
              >
                Sair
              </Botao>
            </>
          ) : (
            <>
              <BotaoLink variante="secundario" href="/login">
                Entrar
              </BotaoLink>
              <BotaoLink href="/cadastro">Criar conta</BotaoLink>
            </>
          )}
        </div>
      </div>
      {usuario?.papel === 'CLIENTE' && <FaixaDeReserva />}
    </header>
  );
}
