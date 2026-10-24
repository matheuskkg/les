import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { CampoDeFormulario } from '../componentes/CampoDeFormulario';
import { Erro } from '../componentes/Erro';
import { GraficoDeVendas, type SerieDoGrafico } from '../componentes/GraficoDeVendas';
import { OpcoesMarcaveis } from '../componentes/OpcoesMarcaveis';
import { Violacoes } from '../componentes/Violacoes';
import {
  consultaDaAnalise,
  corDaCategoria,
  lerEstadoDaAnalise,
  rotuloDoMes,
  urlDaAnalise,
  urlDaExportacao,
  type EstadoDaAnalise,
} from '../filtrosDaAnalise';
import { formatarDinheiro } from '../formatacao';

type Analise = components['schemas']['AnaliseDeVendasDto'];
type Categoria = components['schemas']['CategoriaDto'];

interface Resultado {
  chave: string;
  analise: Analise | null;
  problema: ProblemaDetalhe | null;
}

/**
 * Análise de vendas do administrador (RF-ANA-001 a RF-ANA-004): período em meses e categorias na URL, gráfico de
 * linhas com uma linha por categoria, tabela com os mesmos valores e exportação em CSV. As regras do período e da
 * seleção são do server, que responde 422 com o requisito violado.
 */
export default function AdminAnalise() {
  const { url, route } = useLocation();
  const busca = url.includes('?') ? url.slice(url.indexOf('?')) : '';
  const estado = useMemo(() => lerEstadoDaAnalise(busca, new Date()), [busca]);
  const [catalogo, definirCatalogo] = useState<Categoria[] | null>(null);
  const [problemaDoCatalogo, definirProblemaDoCatalogo] = useState<ProblemaDetalhe | null>(null);
  const [filtros, definirFiltros] = useState<EstadoDaAnalise>(estado);
  const [semCategoria, definirSemCategoria] = useState(false);
  const [resultado, definirResultado] = useState<Resultado | null>(null);


  useEffect(() => {
    const controle = new AbortController();
    cliente
      .GET('/api/livros/categorias', { params: { query: { tamanho: 100 } }, signal: controle.signal })
      .then(({ data, error, response }) => {
        if (!controle.signal.aborted) {
          if (response.ok) {
            definirCatalogo(data?.itens ?? []);
          } else {
            definirProblemaDoCatalogo(interpretarProblema(error, response.status));
          }
        }
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirProblemaDoCatalogo({ status: 0, detail: 'Não foi possível carregar as categorias. Tente novamente.' });
        }
      });
    return () => controle.abort();
  }, []);

  const idsDoCatalogo = useMemo(() => (catalogo ?? []).map((categoria) => categoria.id), [catalogo]);

  // O formulário mostra o que está na URL; sem categoria na URL, todas aparecem marcadas.
  useEffect(() => {
    definirFiltros({ ...estado, categorias: estado.categorias.length > 0 ? estado.categorias : idsDoCatalogo });
    definirSemCategoria(false);
  }, [estado, idsDoCatalogo]);
  const consulta = useMemo(
    () => (catalogo === null ? null : consultaDaAnalise(estado, idsDoCatalogo)),
    [catalogo, estado, idsDoCatalogo],
  );
  const chave = consulta === null ? '' : urlDaExportacao(consulta);

  useEffect(() => {
    if (consulta === null) {
      return;
    }
    const controle = new AbortController();
    cliente
      .GET('/api/admin/analise', { params: { query: consulta }, signal: controle.signal })
      .then(({ data, error, response }) => {
        if (!controle.signal.aborted) {
          definirResultado(
            response.ok
              ? { chave, analise: data ?? null, problema: null }
              : { chave, analise: null, problema: interpretarProblema(error, response.status) },
          );
        }
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirResultado({
            chave,
            analise: null,
            problema: { status: 0, detail: 'Não foi possível carregar a análise. Tente novamente.' },
          });
        }
      });
    return () => controle.abort();
  }, [consulta, chave]);

  const visivel = resultado !== null && resultado.chave === chave ? resultado : null;
  const analise = visivel?.analise ?? null;
  const meses = useMemo(() => (analise?.meses ?? []).map(rotuloDoMes), [analise]);
  const series = useMemo<SerieDoGrafico[]>(
    () =>
      (analise?.series ?? []).map((serie) => ({
        nome: serie.categoria,
        cor: corDaCategoria(serie.categoriaId, idsDoCatalogo),
        valores: serie.valores,
      })),
    [analise, idsDoCatalogo],
  );
  const opcoes = (catalogo ?? []).map((categoria) => ({ valor: String(categoria.id), rotulo: categoria.nome }));

  function filtrar(evento: Event) {
    evento.preventDefault();
    if (filtros.categorias.length === 0) {
      definirSemCategoria(true);
      return;
    }
    route(urlDaAnalise(filtros));
  }

  function alternarCategoria(valor: string) {
    const id = Number(valor);
    definirSemCategoria(false);
    definirFiltros((atual) => ({
      ...atual,
      categorias: idsDoCatalogo.filter((outro) => (outro === id) !== atual.categorias.includes(outro)),
    }));
  }

  return (
    <section class="pagina">
      <header>
        <h1>Análise de vendas</h1>
        <p class="pagina__introducao">
          Vendas por categoria em cada mês, sem frete e descontadas as trocas concluídas. Contam os pedidos aprovados,
          em transporte e entregues.
        </p>
      </header>

      <Erro problema={problemaDoCatalogo} />

      <form class="cartao vitrine__filtros" aria-label="Filtrar análise" onSubmit={filtrar}>
        <CampoDeFormulario id="analise-inicio" rotulo="Mês inicial" tipo="month" name="inicio" required
          valor={filtros.inicio} aoMudar={(valor) => definirFiltros((atual) => ({ ...atual, inicio: valor }))} />
        <CampoDeFormulario id="analise-fim" rotulo="Mês final" tipo="month" name="fim" required
          valor={filtros.fim} aoMudar={(valor) => definirFiltros((atual) => ({ ...atual, fim: valor }))} />
        <OpcoesMarcaveis legenda="Categorias" nome="categoria" opcoes={opcoes}
          marcados={filtros.categorias.map(String)} alternar={alternarCategoria} />
        {semCategoria && (
          <p class="erro" role="alert">
            Selecione ao menos uma categoria.
          </p>
        )}
        <div class="acoes">
          <button class="botao" type="submit" disabled={catalogo === null}>
            Atualizar
          </button>
          {visivel?.analise && (
            <a class="botao botao--secundario" href={chave} download>
              Exportar CSV
            </a>
          )}
        </div>
      </form>

      {visivel === null ? (
        catalogo !== null && (
          <p class="aviso" role="status">
            Carregando análise…
          </p>
        )
      ) : visivel.problema ? (
        <>
          <Violacoes violacoes={visivel.problema.violacoes} />
          {!visivel.problema.violacoes?.length && <Erro problema={visivel.problema} />}
        </>
      ) : (
        analise && (
          <>
            <div class="cartao">
              <h2>
                Evolução de {rotuloDoMes(analise.inicio)} a {rotuloDoMes(analise.fim)}
              </h2>
              <GraficoDeVendas meses={meses} series={series} />
            </div>
            <div class="tabela-rolavel">
              <table class="tabela" aria-label="Vendas por mês e categoria">
                <thead>
                  <tr>
                    <th scope="col">Mês</th>
                    {analise.series.map((serie) => (
                      <th scope="col" key={serie.categoriaId} class="tabela__numero">
                        {serie.categoria}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {meses.map((mes, indice) => (
                    <tr key={mes}>
                      <th scope="row">{mes}</th>
                      {analise.series.map((serie) => (
                        <td key={serie.categoriaId} class="tabela__numero">
                          {formatarDinheiro(serie.valores[indice] ?? '0')}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )
      )}
    </section>
  );
}
