import { useEffect, useRef } from 'preact/hooks';
import uPlot from 'uplot';
import 'uplot/dist/uPlot.min.css';
import { formatarDinheiro } from '../../../compartilhado/formatacao';

export interface SerieDoGrafico {
  id: number;
  nome: string;
  cor: string;
  /** Valores em decimal da API ("1234.56"), um por mês. */
  valores: string[];
}

interface Props {
  /** Rótulos dos meses do eixo X, já como `mm/aaaa`. */
  meses: string[];
  series: SerieDoGrafico[];
}

const ALTURA = 320;

/** Valor do eixo e do tooltip em reais (RNF-ANA-002); o número só serve para posicionar a linha. */
function emReais(valor: number): string {
  return formatarDinheiro(valor.toFixed(2));
}

/**
 * Tooltip com o valor de todas as categorias no mês sob o cursor (RNF-ANA-004): o valor em destaque e o nome em
 * seguida, com um traço da cor da linha. Os nomes entram por `textContent`.
 */
function tooltip(meses: string[], series: SerieDoGrafico[]): uPlot.Plugin {
  let caixa: HTMLDivElement;
  return {
    hooks: {
      init: (grafico) => {
        caixa = document.createElement('div');
        caixa.className = 'grafico__tooltip';
        caixa.hidden = true;
        grafico.over.appendChild(caixa);
      },
      setCursor: (grafico) => {
        const { idx, left = 0 } = grafico.cursor;
        if (idx === null || idx === undefined) {
          caixa.hidden = true;
          return;
        }
        caixa.replaceChildren();
        const titulo = document.createElement('strong');
        titulo.textContent = meses[idx] ?? '';
        caixa.appendChild(titulo);
        series.forEach((serie, indice) => {
          const linha = document.createElement('div');
          linha.className = 'grafico__tooltip-linha';
          const traco = document.createElement('span');
          traco.className = 'grafico__traco';
          traco.style.background = serie.cor;
          const valor = document.createElement('b');
          valor.textContent = emReais(Number(grafico.data[indice + 1]?.[idx] ?? 0));
          const nome = document.createElement('span');
          nome.textContent = serie.nome;
          linha.append(traco, valor, nome);
          caixa.appendChild(linha);
        });
        caixa.hidden = false;
        const largura = grafico.over.clientWidth;
        caixa.style.left = `${left > largura / 2 ? left - caixa.offsetWidth - 12 : left + 12}px`;
      },
    },
  };
}

/**
 * Gráfico de linhas da evolução de vendas (RNF-ANA-001 a RNF-ANA-004): meses no eixo X, reais no eixo Y com escala
 * automática, uma linha por categoria e legenda abaixo. A tabela da página repete os valores, para quem não usa o
 * cursor ou não distingue as cores.
 */
export function GraficoDeVendas({ meses, series }: Props) {
  const area = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const elemento = area.current;
    if (!elemento) {
      return;
    }
    const dados: uPlot.AlignedData = [
      meses.map((_, indice) => indice),
      ...series.map((serie) => serie.valores.map(Number)),
    ];
    const grafico = new uPlot(
      {
        width: Math.max(elemento.clientWidth, 280),
        height: ALTURA,
        legend: { show: false },
        cursor: { y: false, drag: { x: false, y: false }, points: { size: 8 } },
        scales: { x: { time: false }, y: { range: (_, __, maximo) => [0, maximo > 0 ? maximo * 1.1 : 100] } },
        axes: [
          {
            stroke: '#55606a',
            grid: { show: false },
            // Passos inteiros de meses: com muitos meses, o eixo pula rótulos em vez de sobrepô-los.
            incrs: [1, 2, 3, 4, 6, 12],
            space: 56,
            values: (_, valores) => valores.map((valor) => (Number.isInteger(valor) ? (meses[valor] ?? '') : '')),
          },
          {
            stroke: '#55606a',
            size: 96,
            grid: { stroke: '#ece7dc', width: 1 },
            values: (_, valores) => valores.map(emReais),
          },
        ],
        series: [
          {},
          ...series.map((serie) => ({
            label: serie.nome,
            stroke: serie.cor,
            width: 2,
            points: { show: meses.length <= 12, size: 8, stroke: serie.cor, fill: '#ffffff', width: 2 },
          })),
        ],
        plugins: [tooltip(meses, series)],
      },
      dados,
      elemento,
    );
    const observador = new ResizeObserver(() => {
      grafico.setSize({ width: Math.max(elemento.clientWidth, 280), height: ALTURA });
    });
    observador.observe(elemento);
    return () => {
      observador.disconnect();
      grafico.destroy();
    };
  }, [meses, series]);

  return (
    <figure class="grafico">
      <div ref={area} class="grafico__area" role="img"
        aria-label="Gráfico de linhas das vendas por categoria em cada mês; os valores estão na tabela abaixo." />
      <figcaption>
        <ul class="grafico__legenda" aria-label="Legenda">
          {series.map((serie) => (
            <li key={serie.id}>
              <span class="grafico__traco" style={{ background: serie.cor }} aria-hidden="true" />
              {serie.nome}
            </li>
          ))}
        </ul>
      </figcaption>
    </figure>
  );
}
