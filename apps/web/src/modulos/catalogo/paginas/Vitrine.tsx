import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { formatarDinheiro } from '../../../compartilhado/formatacao';
import { useCarregamento } from '../../../compartilhado/useCarregamento';
import { Botao } from '../../../ui/Botao';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { CapaDeLivro } from '../../../ui/CapaDeLivro';
import { Erro } from '../../../ui/Erro';
import { Esqueleto } from '../../../ui/Esqueleto';
import { Paginacao } from '../../../ui/Paginacao';
import { SeletorDeOrdem } from '../../../ui/SeletorDeOrdem';
import {
  ORDENS,
  PAGINA_PADRAO,
  consultaDoEstado,
  filtrosEmUso,
  filtrosVazios,
  lerEstadoDaVitrine,
  urlDoEstado,
  type CampoDeTexto,
  type EstadoDaVitrine,
  type FiltrosDeLivros,
} from '../regras/filtrosDeLivros';

type Categoria = components['schemas']['CategoriaDto'];
type LivroListado = components['schemas']['LivroListadoDto'];

/** Campos técnicos, recolhidos em "Mais filtros" e aplicados pelo botão Filtrar. */
const CAMPOS_AVANCADOS: readonly { campo: CampoDeTexto; rotulo: string; placeholder?: string }[] = [
  { campo: 'titulo', rotulo: 'Título' },
  { campo: 'autor', rotulo: 'Autor' },
  { campo: 'editora', rotulo: 'Editora' },
  { campo: 'isbn', rotulo: 'ISBN' },
  { campo: 'codigo', rotulo: 'Código' },
  { campo: 'anoDe', rotulo: 'Ano de', placeholder: 'Ex.: 1990' },
  { campo: 'anoAte', rotulo: 'Ano até', placeholder: 'Ex.: 2026' },
];

function CartaoDeLivro({ livro }: { livro: LivroListado }) {
  const disponivel = livro.disponivel > 0;

  return (
    <article class={`livro${disponivel ? '' : ' livro--esgotado'}`}>
      <a class="livro__capa" href={`/livros/${livro.id}`} tabIndex={-1} aria-hidden="true">
        <CapaDeLivro titulo={livro.titulo} autores={livro.autores} categoria={livro.categorias[0]?.nome} />
      </a>
      <h2 class="livro__titulo">
        <a href={`/livros/${livro.id}`}>{livro.titulo}</a>
      </h2>
      <p class="livro__autores">{livro.autores.join('; ')}</p>
      <p class="livro__meta">
        {livro.editora} · {livro.ano}
      </p>
      <p class="livro__preco">{livro.precoVenda ? formatarDinheiro(livro.precoVenda) : 'Indisponível'}</p>
      <p class="livro__estoque">
        {disponivel ? (livro.disponivel <= 3 ? `Últimas ${livro.disponivel} unidades` : 'Em estoque') : 'Esgotado'}
      </p>
    </article>
  );
}

function Categorias({
  categorias,
  marcadas,
  alternar,
}: {
  categorias: Categoria[];
  marcadas: string[];
  alternar: (id: string) => void;
}) {
  if (categorias.length === 0) {
    return null;
  }
  return (
    <fieldset class="grupo-de-categorias">
      <legend>Categorias</legend>
      {categorias.map((categoria) => {
        const id = String(categoria.id);
        return (
          <label class="categoria-opcao" key={categoria.id}>
            <input
              type="checkbox"
              name="categoria"
              value={id}
              checked={marcadas.includes(id)}
              onChange={() => alternar(id)}
            />
            {categoria.nome}
          </label>
        );
      })}
    </fieldset>
  );
}

/**
 * Vitrine (RF-LIV-005): a busca livre fica no cabeçalho, as categorias e a ordenação se aplicam na hora e os
 * campos técnicos ficam em "Mais filtros". A URL guarda toda a busca, para compartilhar e voltar.
 */
export default function Vitrine() {
  const { url, route } = useLocation();
  const busca = url.includes('?') ? url.slice(url.indexOf('?')) : '';
  const estado = useMemo(() => lerEstadoDaVitrine(busca), [busca]);
  const [filtros, definirFiltros] = useState<FiltrosDeLivros>(estado.filtros);

  // Links compartilhados e o voltar do navegador: os campos acompanham a URL.
  useEffect(() => definirFiltros(estado.filtros), [estado]);

  const livros = useCarregamento(
    (signal) => cliente.GET('/api/livros', { params: { query: consultaDoEstado(estado) }, signal }),
    [busca],
  );
  const categorias = useCarregamento(
    (signal) => cliente.GET('/api/livros/categorias', { params: { query: { tamanho: 100 } }, signal }),
    [],
  );

  function ir(novo: EstadoDaVitrine) {
    route(urlDoEstado(novo));
  }

  function alternarCategoria(id: string) {
    const marcadas = estado.filtros.categorias.includes(id)
      ? estado.filtros.categorias.filter((categoria) => categoria !== id)
      : [...estado.filtros.categorias, id];
    ir({ ...estado, filtros: { ...estado.filtros, categorias: marcadas }, pagina: PAGINA_PADRAO });
  }

  function filtrar(evento: Event) {
    evento.preventDefault();
    ir({ ...estado, filtros: { ...filtros, q: estado.filtros.q, categorias: estado.filtros.categorias }, pagina: PAGINA_PADRAO });
  }

  const avancadosEmUso = CAMPOS_AVANCADOS.filter(({ campo }) => estado.filtros[campo] !== '').length;
  const pagina = livros.dados;

  return (
    <section class="pagina vitrine">
      <header class="vitrine__topo">
        <h1>{estado.filtros.q ? `Resultados para “${estado.filtros.q}”` : 'Vitrine'}</h1>
        {filtrosEmUso(estado.filtros) > 0 && (
          <Botao variante="discreto" onClick={() => route('/')}>
            Limpar filtros
          </Botao>
        )}
      </header>

      <div class="vitrine__corpo">
        <aside class="vitrine__lateral" aria-label="Filtros">
          <Categorias
            categorias={categorias.dados?.itens ?? []}
            marcadas={estado.filtros.categorias}
            alternar={alternarCategoria}
          />

          <details class="mais-filtros" open={avancadosEmUso > 0}>
            <summary>Mais filtros{avancadosEmUso > 0 && ` (${avancadosEmUso})`}</summary>
            <form role="search" aria-label="Filtrar livros" onSubmit={filtrar}>
              {CAMPOS_AVANCADOS.map(({ campo, rotulo, placeholder }) => (
                <CampoDeFormulario
                  key={campo}
                  id={`filtro-${campo}`}
                  rotulo={rotulo}
                  name={campo}
                  placeholder={placeholder}
                  valor={filtros[campo]}
                  aoMudar={(valor) => definirFiltros((atual) => ({ ...atual, [campo]: valor }))}
                />
              ))}
              <div class="acoes">
                <Botao type="submit">Filtrar</Botao>
                <Botao
                  variante="secundario"
                  onClick={() => {
                    const vazios = filtrosVazios();
                    ir({ ...estado, filtros: { ...vazios, q: estado.filtros.q, categorias: estado.filtros.categorias }, pagina: PAGINA_PADRAO });
                  }}
                >
                  Limpar
                </Botao>
              </div>
            </form>
          </details>
        </aside>

        <div class="vitrine__resultado">
          <div class="resultado">
            <p class="resultado__total" role="status">
              {pagina === null
                ? livros.carregando
                  ? 'Buscando livros…'
                  : ''
                : pagina.totalItens === 1
                  ? '1 livro encontrado'
                  : `${pagina.totalItens} livros encontrados`}
            </p>
            <SeletorDeOrdem
              id="ordem-dos-livros"
              valor={estado.ordem}
              aoMudar={(ordem) => ir({ ...estado, ordem, pagina: PAGINA_PADRAO })}
              opcoes={ORDENS}
            />
          </div>

          <Erro problema={livros.problema} />

          {pagina === null ? (
            livros.carregando && <Esqueleto variante="grade" rotulo="Carregando livros…" />
          ) : pagina.itens.length === 0 ? (
            <div class="cartao vitrine__vazia">
              <p>Nenhum livro encontrado.</p>
              <p class="campo__ajuda">Tente outra palavra ou desmarque algum filtro.</p>
            </div>
          ) : (
            <>
              <ul class={`grade-de-livros${livros.carregando ? ' grade-de-livros--atualizando' : ''}`}>
                {pagina.itens.map((livro) => (
                  <li key={livro.id}>
                    <CartaoDeLivro livro={livro} />
                  </li>
                ))}
              </ul>
              <Paginacao
                pagina={estado.pagina}
                totalPaginas={pagina.totalPaginas}
                rotulo="Paginação da vitrine"
                urlDaPagina={(numero) => urlDoEstado({ ...estado, pagina: numero })}
              />
            </>
          )}
        </div>
      </div>
    </section>
  );
}
