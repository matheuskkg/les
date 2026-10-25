import { useEffect, useRef } from 'preact/hooks';
import uPlot from 'uplot';
import 'uplot/dist/uPlot.min.css';
import { COR_DAS_CHAMADAS, COR_DAS_FALHAS, formatarInteiro } from '../regras/assistenteDoAdmin';

interface Props {
  /** Rótulos dos dias do eixo X, já como `dd/mm`. */
  dias: string[];
  chamadas: number[];
  falhas: number[];
}

const ALTURA = 280;
const SERIES = [
  { nome: 'Chamadas', cor: COR_DAS_CHAMADAS },
  { nome: 'Falhas', cor: COR_DAS_FALHAS },
] as const;

/** Tooltip com as chamadas e as falhas do dia sob o cursor; os textos entram por `textContent`. */
function tooltip(dias: string[]): uPlot.Plugin {
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
        titulo.textContent = dias[idx] ?? '';
        caixa.appendChild(titulo);
        SERIES.forEach((serie, indice) => {
          const linha = document.createElement('div');
          linha.className = 'grafico__tooltip-linha';
          const traco = document.createElement('span');
          traco.className = 'grafico__traco';
          traco.style.background = serie.cor;
          const valor = document.createElement('b');
          valor.textContent = formatarInteiro(Number(grafico.data[indice + 1]?.[idx] ?? 0));
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
 * Gráfico de linhas das chamadas à IA e das falhas por dia, somando todos os provedores (RNF-REC-001, DEC-03): um
 * só eixo Y com contagens inteiras e legenda abaixo. A tabela de dias da página repete os valores.
 */
export function GraficoDeChamadas({ dias, chamadas, falhas }: Props) {
  const area = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const elemento = area.current;
    if (!elemento) {
      return;
    }
    const dados: uPlot.AlignedData = [dias.map((_, indice) => indice), chamadas, falhas];
    const grafico = new uPlot(
      {
        width: Math.max(elemento.clientWidth, 280),
        height: ALTURA,
        legend: { show: false },
        cursor: { y: false, drag: { x: false, y: false }, points: { size: 8 } },
        scales: { x: { time: false }, y: { range: (_, __, maximo) => [0, maximo > 0 ? Math.ceil(maximo * 1.1) : 5] } },
        axes: [
          {
            stroke: '#55606a',
            grid: { show: false },
            incrs: [1, 2, 7, 14, 30, 60],
            space: 48,
            values: (_, valores) => valores.map((valor) => (Number.isInteger(valor) ? (dias[valor] ?? '') : '')),
          },
          {
            stroke: '#55606a',
            size: 56,
            grid: { stroke: '#ece7dc', width: 1 },
            // Só passos inteiros: não existe meia chamada.
            incrs: [1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000],
            values: (_, valores) => valores.map(formatarInteiro),
          },
        ],
        series: [
          {},
          ...SERIES.map((serie) => ({
            label: serie.nome,
            stroke: serie.cor,
            width: 2,
            points: { show: dias.length <= 31, size: 8, stroke: serie.cor, fill: '#ffffff', width: 2 },
          })),
        ],
        plugins: [tooltip(dias)],
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
  }, [dias, chamadas, falhas]);

  return (
    <figure class="grafico">
      <div ref={area} class="grafico__area grafico__area--chamadas" role="img"
        aria-label="Gráfico de linhas das chamadas à IA e das falhas em cada dia; os valores estão na tabela abaixo." />
      <figcaption>
        <ul class="grafico__legenda" aria-label="Legenda">
          {SERIES.map((serie) => (
            <li key={serie.nome}>
              <span class="grafico__traco" style={{ background: serie.cor }} aria-hidden="true" />
              {serie.nome}
            </li>
          ))}
        </ul>
      </figcaption>
    </figure>
  );
}
