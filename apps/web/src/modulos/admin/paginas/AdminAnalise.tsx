import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { Erro } from '../../../ui/Erro';
import { GraficoDeVendas, type SerieDoGrafico } from '../componentes/GraficoDeVendas';
import { OpcoesMarcaveis } from '../../../ui/OpcoesMarcaveis';
import { Violacoes } from '../../../ui/Violacoes';
import {
  consultaDaAnalise,
  corDaCategoria,
  lerEstadoDaAnalise,
  rotuloDoDia,
  rotuloDoMes,
  urlDaAnalise,
  urlDaExportacao,
  type EstadoDaAnalise,
} from '../regras/filtrosDaAnalise';
import { formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';

type Analise = components['schemas']['AnaliseDeVendasDto'];
type Categoria = components['schemas']['CategoriaDto'];

interface Resultado {
  chave: string;
  analise: Analise | null;
  problema: ProblemaDetalhe | null;
}

/**
 * Análise de vendas do administrador (RF-ANA-001 a RF-ANA-004): datas exatas e categorias na URL, gráfico de linhas
 * com um ponto por mês e uma linha por categoria, tabela com os mesmos valores e exportação em CSV. O filtro é por dia,
 * mas a agregação continua mensal (DEC-19). As regras do período e da seleção são do server, que responde 422 com o
 * requisito violado.
 */
class ProblemaDoCatalogo extends Error {
  constructor(readonly problema: ProblemaDetalhe) {
    super(problema.detail);
  }
}

/** Todas as categorias do catálogo, página a página (o tamanho máximo da página é 100, DEC-07). */
async function carregarCatalogo(signal: AbortSignal): Promise<Categoria[]> {
  const categorias: Categoria[] = [];
  for (let pagina = 0; ; pagina++) {
    const { data, error, response } = await cliente.GET('/api/livros/categorias', {
      params: { query: { pagina, tamanho: 100, ordem: ['nome,asc'] } },
      signal,
    });
    if (!response.ok || !data) {
      throw new ProblemaDoCatalogo(interpretarProblema(error, response.status));
    }
    categorias.push(...data.itens);
    if (pagina + 1 >= data.totalPaginas) {
      return categorias;
    }
  }
}

export default function AdminAnalise() {
  const { url, route } = useLocation();
  const busca = url.includes('?') ? url.slice(url.indexOf('?')) : '';
  const estado = useMemo(() => lerEstadoDaAnalise(busca, new Date()), [busca]);
  const [catalogo, definirCatalogo] = useState<Categoria[] | null>(null);
  const [problemaDoCatalogo, definirProblemaDoCatalogo] = useState<ProblemaDetalhe | null>(null);
  const [filtros, definirFiltros] = useState<EstadoDaAnalise>(estado);
  const [semCategoria, definirSemCategoria] = useState(false);
  const [resultado, definirResultado] = useState<Resultado | null>(null);
  const [exportando, definirExportando] = useState(false);
  const [problemaDaExportacao, definirProblemaDaExportacao] = useState<ProblemaDetalhe | null>(null);


  useEffect(() => {
    const controle = new AbortController();
    carregarCatalogo(controle.signal)
      .then((categorias) => {
        if (!controle.signal.aborted) {
          definirCatalogo(categorias);
        }
      })
      .catch((erro: unknown) => {
        if (!controle.signal.aborted) {
          definirProblemaDoCatalogo(
            erro instanceof ProblemaDoCatalogo
              ? erro.problema
              : { status: 0, detail: 'Não foi possível carregar as categorias. Tente novamente.' },
          );
        }
      });
    return () => controle.abort();
  }, []);

  const idsDoCatalogo = useMemo(() => (catalogo ?? []).map((categoria) => categoria.id), [catalogo]);

  // O formulário mostra o que está na URL; sem categoria na URL, todas aparecem marcadas.
  useEffect(() => {
    definirFiltros({ ...estado, categorias: estado.categorias.length > 0 ? estado.categorias : idsDoCatalogo });
    definirSemCategoria(false);
    definirProblemaDaExportacao(null);
    // idsDoCatalogo fica de fora: a chegada do catálogo só marca as categorias, sem desfazer as datas digitadas.
  }, [estado]);

  useEffect(() => {
    if (estado.categorias.length === 0) {
      definirFiltros((atual) => ({ ...atual, categorias: idsDoCatalogo }));
    }
  }, [idsDoCatalogo]);

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
        id: serie.categoriaId,
        nome: serie.categoria,
        cor: corDaCategoria(serie.categoriaId, idsDoCatalogo),
        valores: serie.valores,
      })),
    [analise, idsDoCatalogo],
  );
  const opcoes = (catalogo ?? []).map((categoria) => ({ valor: String(categoria.id), rotulo: categoria.nome }));

  /**
   * Baixa o CSV pelo cliente da API, e não por um link direto, para que um erro (sessão expirada, regra violada)
   * apareça na tela em vez de ser salvo como planilha (RF-ANA-004).
   */
  async function exportar() {
    if (consulta === null) {
      return;
    }
    definirExportando(true);
    definirProblemaDaExportacao(null);
    try {
      const { data, error, response } = await cliente.GET('/api/admin/analise/exportacao', {
        params: { query: consulta },
        parseAs: 'blob',
      });
      if (!response.ok || !data) {
        definirProblemaDaExportacao(interpretarProblema(error, response.status));
        return;
      }
      const endereco = URL.createObjectURL(data);
      const link = document.createElement('a');
      link.href = endereco;
      link.download = `analise-de-vendas-${consulta.inicio}-a-${consulta.fim}.csv`;
      link.click();
      URL.revokeObjectURL(endereco);
    } catch {
      definirProblemaDaExportacao({ status: 0, detail: 'Não foi possível exportar a análise. Tente novamente.' });
    } finally {
      definirExportando(false);
    }
  }

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
          Escolha as datas e as categorias: o gráfico mostra cada mês do período, com as vendas dos dias selecionados
          nos meses das bordas. Sem frete, sem abater o desconto promocional e descontadas as trocas concluídas. Contam
          os pedidos aprovados, em transporte e entregues.
        </p>
      </header>

      <Erro problema={problemaDoCatalogo} />

      <form class="cartao filtros" aria-label="Filtrar análise" onSubmit={filtrar}>
        <CampoDeFormulario id="analise-inicio" rotulo="Data inicial" tipo="date" name="inicio" required
          valor={filtros.inicio} aoMudar={(valor) => definirFiltros((atual) => ({ ...atual, inicio: valor }))} />
        <CampoDeFormulario id="analise-fim" rotulo="Data final" tipo="date" name="fim" required
          valor={filtros.fim} aoMudar={(valor) => definirFiltros((atual) => ({ ...atual, fim: valor }))} />
        <OpcoesMarcaveis legenda="Categorias" nome="categoria" opcoes={opcoes}
          marcados={filtros.categorias.map(String)} alternar={alternarCategoria} />
        {semCategoria && (
          <p class="erro" role="alert">
            Selecione ao menos uma categoria.
          </p>
        )}
        <div class="acoes">
          <Botao type="submit" disabled={catalogo === null}>
            Atualizar
          </Botao>
          {visivel?.analise && (
            <Botao variante="secundario" type="button" disabled={exportando} onClick={exportar}>
              {exportando ? 'Exportando…' : 'Exportar CSV'}
            </Botao>
          )}
        </div>
        <Erro problema={problemaDaExportacao} />
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
                Evolução de {rotuloDoDia(analise.inicio)} a {rotuloDoDia(analise.fim)}
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
