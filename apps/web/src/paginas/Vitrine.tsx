import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { CampoDeFormulario } from '../componentes/CampoDeFormulario';
import { Erro } from '../componentes/Erro';
import { formatarDinheiro } from '../formatacao';
import {
  ORDENS,
  PAGINA_PADRAO,
  consultaDoEstado,
  filtrosVazios,
  lerEstadoDaVitrine,
  urlDoEstado,
  type CampoDeTexto,
  type FiltrosDeLivros,
} from '../filtrosDeLivros';

type Categoria = components['schemas']['CategoriaDto'];
type PaginaDeLivros = components['schemas']['PaginaLivroListadoDto'];
type LivroListado = components['schemas']['LivroListadoDto'];

/** Resultado da consulta, preso à busca que o pediu para não exibir dados de outra URL. */
interface ResultadoDaVitrine {
  busca: string;
  pagina: PaginaDeLivros | null;
  problema: ProblemaDetalhe | null;
  falha: string | null;
}

function CartaoDeLivro({ livro }: { livro: LivroListado }) {
  const disponivel = livro.disponivel > 0;

  return (
    <article class={`cartao livro${disponivel ? '' : ' livro--esgotado'}`}>
      <h2 class="livro__titulo">
        <a href={`/livros/${livro.id}`}>{livro.titulo}</a>
      </h2>
      <p class="livro__autores">{livro.autores.join('; ')}</p>
      <p class="livro__meta">
        {livro.editora} · {livro.ano}
      </p>
      {livro.categorias.length > 0 && (
        <ul class="livro__categorias" aria-label="Categorias">
          {livro.categorias.map((categoria) => (
            <li key={categoria.id}>{categoria.nome}</li>
          ))}
        </ul>
      )}
      <p class="livro__preco">
        {livro.precoVenda ? formatarDinheiro(livro.precoVenda) : 'Indisponível'}
      </p>
      <p class="livro__estoque">
        {disponivel ? `Em estoque: ${livro.disponivel}` : 'Esgotado'}
      </p>
    </article>
  );
}

function BotaoDePagina({ rotulo, destino }: { rotulo: string; destino: string | null }) {
  if (destino === null) {
    return (
      <span class="botao botao--secundario botao--inativo" aria-disabled="true">
        {rotulo}
      </span>
    );
  }

  return (
    <a class="botao botao--secundario" href={destino}>
      {rotulo}
    </a>
  );
}

export default function Vitrine() {
  const { url, route } = useLocation();
  const busca = url.includes('?') ? url.slice(url.indexOf('?')) : '';
  const estado = useMemo(() => lerEstadoDaVitrine(busca), [busca]);

  const [filtros, definirFiltros] = useState<FiltrosDeLivros>(estado.filtros);
  const [categorias, definirCategorias] = useState<Categoria[]>([]);
  const [resultado, definirResultado] = useState<ResultadoDaVitrine | null>(null);

  // Links compartilhados e botão voltar do navegador: o formulário acompanha a URL.
  useEffect(() => {
    definirFiltros(estado.filtros);
  }, [estado]);

  useEffect(() => {
    const controle = new AbortController();
    const buscaPedida = busca;

    async function carregar(): Promise<void> {
      try {
        const { data, error, response } = await cliente.GET('/api/livros', {
          params: { query: consultaDoEstado(estado) },
          signal: controle.signal,
        });
        if (controle.signal.aborted) {
          return;
        }
        if (!response.ok) {
          definirResultado({
            busca: buscaPedida,
            pagina: null,
            problema: interpretarProblema(error, response.status),
            falha: null,
          });
          return;
        }
        definirResultado({ busca: buscaPedida, pagina: data ?? null, problema: null, falha: null });
      } catch {
        if (!controle.signal.aborted) {
          definirResultado({
            busca: buscaPedida,
            pagina: null,
            problema: null,
            falha: 'Não foi possível carregar os livros. Tente novamente.',
          });
        }
      }
    }

    void carregar();
    return () => controle.abort();
  }, [busca, estado]);

  useEffect(() => {
    const controle = new AbortController();

    cliente
      .GET('/api/livros/categorias', {
        params: { query: { tamanho: 100 } },
        signal: controle.signal,
      })
      .then(({ data }) => {
        if (!controle.signal.aborted) {
          definirCategorias(data?.itens ?? []);
        }
      })
      .catch(() => {
        // Sem categorias a vitrine continua funcionando; o filtro fica vazio.
      });

    return () => controle.abort();
  }, []);

  function mudarCampo(campo: CampoDeTexto, valor: string): void {
    definirFiltros((atual) => ({ ...atual, [campo]: valor }));
  }

  function alternarCategoria(id: string): void {
    definirFiltros((atual) => ({
      ...atual,
      categorias: atual.categorias.includes(id)
        ? atual.categorias.filter((categoria) => categoria !== id)
        : [...atual.categorias, id],
    }));
  }

  function filtrar(evento: Event): void {
    evento.preventDefault();
    route(urlDoEstado({ filtros, ordem: estado.ordem, pagina: PAGINA_PADRAO }));
  }

  function limpar(): void {
    definirFiltros(filtrosVazios());
    route('/');
  }

  function mudarOrdem(ordem: string): void {
    route(urlDoEstado({ ...estado, ordem, pagina: PAGINA_PADRAO }));
  }

  const visivel = resultado !== null && resultado.busca === busca ? resultado : null;
  const pagina = visivel?.pagina ?? null;
  const problema = visivel?.problema ?? null;
  const falha = visivel?.falha ?? null;

  const anteriores =
    estado.pagina > PAGINA_PADRAO
      ? urlDoEstado({ ...estado, pagina: estado.pagina - 1 })
      : null;
  const proximas =
    pagina !== null && estado.pagina < pagina.totalPaginas
      ? urlDoEstado({ ...estado, pagina: estado.pagina + 1 })
      : null;

  return (
    <section class="pagina">
      <header>
        <h1>Vitrine</h1>
        <p class="pagina__introducao">
          Consulte o acervo por título, autor, editora, ISBN, código, ano ou categoria. Os filtros
          informados se combinam, e o endereço da página guarda a sua busca.
        </p>
      </header>

      <form class="cartao vitrine__filtros" role="search" aria-label="Filtrar livros" onSubmit={filtrar}>
        <CampoDeFormulario
          id="filtro-titulo"
          rotulo="Título"
          valor={filtros.titulo}
          aoMudar={(valor) => mudarCampo('titulo', valor)}
          name="titulo"
        />
        <CampoDeFormulario
          id="filtro-autor"
          rotulo="Autor"
          valor={filtros.autor}
          aoMudar={(valor) => mudarCampo('autor', valor)}
          name="autor"
        />
        <CampoDeFormulario
          id="filtro-editora"
          rotulo="Editora"
          valor={filtros.editora}
          aoMudar={(valor) => mudarCampo('editora', valor)}
          name="editora"
        />
        <CampoDeFormulario
          id="filtro-isbn"
          rotulo="ISBN"
          valor={filtros.isbn}
          aoMudar={(valor) => mudarCampo('isbn', valor)}
          name="isbn"
        />
        <CampoDeFormulario
          id="filtro-codigo"
          rotulo="Código"
          valor={filtros.codigo}
          aoMudar={(valor) => mudarCampo('codigo', valor)}
          ajuda="Número do cadastro do livro."
          name="codigo"
        />
        <CampoDeFormulario
          id="filtro-ano-de"
          rotulo="Ano de"
          valor={filtros.anoDe}
          aoMudar={(valor) => mudarCampo('anoDe', valor)}
          placeholder="Ex.: 1990"
          name="anoDe"
        />
        <CampoDeFormulario
          id="filtro-ano-ate"
          rotulo="Ano até"
          valor={filtros.anoAte}
          aoMudar={(valor) => mudarCampo('anoAte', valor)}
          placeholder="Ex.: 2026"
          name="anoAte"
        />

        {categorias.length > 0 && (
          <fieldset class="grupo-de-categorias">
            <legend>Categorias</legend>
            <div class="categorias">
              {categorias.map((categoria) => {
                const id = String(categoria.id);
                return (
                  <label class="categoria-opcao" key={categoria.id}>
                    <input
                      type="checkbox"
                      name="categoria"
                      value={id}
                      checked={filtros.categorias.includes(id)}
                      onChange={() => alternarCategoria(id)}
                    />
                    {categoria.nome}
                  </label>
                );
              })}
            </div>
          </fieldset>
        )}

        <div class="acoes">
          <button class="botao" type="submit">
            Filtrar
          </button>
          <button class="botao botao--secundario" type="button" onClick={limpar}>
            Limpar filtros
          </button>
        </div>
      </form>

      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando livros…
        </p>
      ) : (
        <>
          <Erro problema={problema} mensagem={falha} />

          {pagina !== null && problema === null && falha === null && (
            <>
              <div class="resultado">
                <p class="resultado__total" role="status">
                  {pagina.totalItens === 1
                    ? '1 livro encontrado'
                    : `${pagina.totalItens} livros encontrados`}
                </p>
                <label class="resultado__ordem">
                  Ordenar por{' '}
                  <select
                    class="campo__entrada"
                    value={estado.ordem}
                    onChange={(evento) => mudarOrdem(evento.currentTarget.value)}
                  >
                    {ORDENS.map((ordem) => (
                      <option key={ordem.valor} value={ordem.valor}>
                        {ordem.rotulo}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              {pagina.itens.length === 0 ? (
                <p class="aviso">Nenhum livro encontrado.</p>
              ) : (
                <>
                  <ul class="grade-de-livros">
                    {pagina.itens.map((livro) => (
                      <li key={livro.id}>
                        <CartaoDeLivro livro={livro} />
                      </li>
                    ))}
                  </ul>

                  <nav class="paginacao" aria-label="Paginação da vitrine">
                    <BotaoDePagina rotulo="Anterior" destino={anteriores} />
                    <p class="paginacao__posicao">
                      Página {estado.pagina} de {Math.max(pagina.totalPaginas, 1)}
                    </p>
                    <BotaoDePagina rotulo="Próxima" destino={proximas} />
                  </nav>
                </>
              )}
            </>
          )}
        </>
      )}
    </section>
  );
}
