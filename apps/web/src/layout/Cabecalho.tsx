import { useEffect, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { itemAtivoDaNavbar, type ItemDaNavbar } from './regras/itemAtivo';
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

function BotaoDoCarrinho({ ativo }: { ativo: boolean }) {
  const { carrinho, abrirGaveta } = useCarrinho();
  const quantidade = carrinho?.quantidadeDeItens ?? 0;
  return (
    <button
      type="button"
      class={`navegacao__link botao-do-carrinho${ativo ? ' navegacao__link--ativo' : ''}`}
      aria-haspopup="dialog"
      aria-current={ativo ? 'page' : undefined}
      aria-label={quantidade > 0 ? `Carrinho: ${quantidade} ${quantidade === 1 ? 'item' : 'itens'}` : 'Carrinho'}
      onClick={abrirGaveta}
    >
      Carrinho
      {quantidade > 0 && <span class="contador contador--neutro">{quantidade}</span>}
    </button>
  );
}

/**
 * Faixa com a contagem regressiva da reserva (RNF-VEN-001): a da compra em andamento, que leva de volta ao pagamento
 * (DEC-12), ou a do carrinho enquanto há itens nele.
 */
function FaixaDeReserva() {
  const { path } = useLocation();
  const { carrinho } = useCarrinho();
  if (carrinho?.checkout && path !== '/carrinho' && path !== '/checkout') {
    return (
      <div class="faixa-de-reserva">
        <div class="faixa-de-reserva__conteudo">
          <PrazoDaReserva expiraEm={carrinho.checkout.expiraEm} prefixo="Sua compra está reservada por" />
          <a href="/checkout">Continuar pagamento</a>
        </div>
      </div>
    );
  }
  if (
    path === '/carrinho' ||
    path === '/checkout' ||
    !carrinho?.expiraEm ||
    carrinho.itens.length === 0
  ) {
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

/** Classe de um item da navegação, com o destaque do item ativo e qualquer classe própria do item. */
function classeDoItem(item: ItemDaNavbar, ativo: ItemDaNavbar | null, classeExtra = '') {
  return `navegacao__link${classeExtra ? ` ${classeExtra}` : ''}${ativo === item ? ' navegacao__link--ativo' : ''}`;
}

export function Cabecalho() {
  const { path } = useLocation();
  const { usuario, carregando, encerrarSessao } = useSessao();
  const { naoLidas } = useNotificacoes();
  const contador = textoDoContador(naoLidas);
  const caminhoDasNotificacoes = usuario ? paginaDeNotificacoes(usuario.papel) : '';
  const ativo = itemAtivoDaNavbar(path, caminhoDasNotificacoes);

  return (
    <header class="cabecalho">
      <div class="cabecalho__faixa">
        <a class="marca" href="/">
          Livraria
        </a>

        {usuario?.papel !== 'ADMIN' && <BuscaGlobal />}

        <nav class="navegacao" aria-label="Principal">
          <a
            class={classeDoItem('vitrine', ativo, 'navegacao__link--inicio')}
            href="/"
            aria-current={ativo === 'vitrine' ? 'page' : undefined}
          >
            Vitrine
          </a>
          {usuario?.papel === 'CLIENTE' && (
            <>
              <BotaoDoCarrinho ativo={ativo === 'carrinho'} />
              <a
                class={classeDoItem('minha-conta', ativo)}
                href="/minha-conta"
                aria-current={ativo === 'minha-conta' ? 'page' : undefined}
              >
                Minha conta
              </a>
            </>
          )}
          {usuario?.papel === 'ADMIN' && (
            <a
              class={classeDoItem('administracao', ativo)}
              href="/admin/clientes"
              aria-current={ativo === 'administracao' ? 'page' : undefined}
            >
              Administração
            </a>
          )}
          {usuario && (
            <a
              class={classeDoItem('notificacoes', ativo)}
              href={caminhoDasNotificacoes}
              aria-current={ativo === 'notificacoes' ? 'page' : undefined}
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
