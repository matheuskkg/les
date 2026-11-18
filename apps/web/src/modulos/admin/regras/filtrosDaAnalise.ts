import type { paths } from '../../../api/esquema';

/** Parâmetros de `GET /api/admin/analise`, os mesmos da exportação (RF-ANA-001 a RF-ANA-004, DEC-15.4, DEC-19). */
export type ConsultaDaAnalise = paths['/api/admin/analise']['get']['parameters']['query'];

/** Meses do período padrão: os 12 últimos, contando o atual (RN-ANA-002 aceita até 24). */
export const MESES_DO_PERIODO_PADRAO = 12;

/**
 * Estado da tela na URL. Datas no formato `AAAA-MM-DD`, ambas inclusivas; sem categoria na URL, todas são analisadas,
 * porque a análise exige ao menos uma (RF-ANA-003). O gráfico continua com um ponto por mês (DEC-19).
 */
export interface EstadoDaAnalise {
  inicio: string;
  fim: string;
  categorias: number[];
}

const FORMATO_DO_DIA = /^\d{4}-\d{2}-\d{2}$/;

/** Confere o formato e também a data: 30/02 e 31/04 não passam. */
function dataValida(texto: string): boolean {
  const [ano = 0, mes = 0, dia = 0] = texto.split('-').map(Number);
  if (ano < 1 || mes < 1 || mes > 12 || dia < 1) {
    return false;
  }
  const data = new Date(0);
  data.setFullYear(ano, mes - 1, dia);
  return data.getFullYear() === ano && data.getMonth() === mes - 1 && data.getDate() === dia;
}

/** Data `AAAA-MM-DD` no fuso do navegador. */
export function diaDe(data: Date): string {
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}`;
}

/** Mês `AAAA-MM` da data, no fuso do navegador. */
export function mesDe(data: Date): string {
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`;
}

/** Soma meses a um `AAAA-MM`, sem passar por `Date` para não depender do fuso. */
export function somarMeses(mes: string, quantidade: number): string {
  const [ano = 0, numero = 1] = mes.split('-').map(Number);
  const total = ano * 12 + (numero - 1) + quantidade;
  return `${Math.floor(total / 12)}-${String((total % 12) + 1).padStart(2, '0')}`;
}

/** Lê o estado da query string; data ausente ou malformada volta ao período padrão, que termina em `hoje`. */
export function lerEstadoDaAnalise(busca: string, hoje: Date): EstadoDaAnalise {
  const parametros = new URLSearchParams(busca);
  const fimPadrao = diaDe(hoje);
  const fim = lerData(parametros.get('fim')) ?? fimPadrao;
  const inicio = lerData(parametros.get('inicio'))
    ?? `${somarMeses(fim.slice(0, 7), 1 - MESES_DO_PERIODO_PADRAO)}-01`;
  const categorias = parametros
    .getAll('categoria')
    .map((valor) => Number(valor.trim()))
    .filter((id, indice, todos) => Number.isSafeInteger(id) && id > 0 && todos.indexOf(id) === indice);
  return { inicio, fim, categorias };
}

function lerData(valor: string | null): string | null {
  const texto = valor?.trim() ?? '';
  return FORMATO_DO_DIA.test(texto) && dataValida(texto) ? texto : null;
}

/** Caminho da tela para o estado. */
export function urlDaAnalise(estado: EstadoDaAnalise): string {
  const parametros = new URLSearchParams({ inicio: estado.inicio, fim: estado.fim });
  estado.categorias.forEach((id) => parametros.append('categoria', String(id)));
  return `/admin/analise?${parametros}`;
}

/** Consulta do contrato: sem categoria escolhida, vão todas as do catálogo, na ordem dele. */
export function consultaDaAnalise(estado: EstadoDaAnalise, todas: readonly number[]): ConsultaDaAnalise {
  return {
    inicio: estado.inicio,
    fim: estado.fim,
    categoria: estado.categorias.length > 0 ? [...estado.categorias] : [...todas],
  };
}

/** Endereço do CSV com os mesmos filtros do gráfico (RF-ANA-004). */
export function urlDaExportacao(consulta: ConsultaDaAnalise): string {
  const parametros = new URLSearchParams({ inicio: consulta.inicio, fim: consulta.fim });
  (consulta.categoria ?? []).forEach((id) => parametros.append('categoria', String(id)));
  return `/api/admin/analise/exportacao?${parametros}`;
}

/** Rótulo da data no cabeçalho: `15/03/2025` (DEC-19). */
export function rotuloDoDia(data: string): string {
  const [ano, mes, dia] = data.split('-');
  return `${dia}/${mes}/${ano}`;
}

/** Rótulo do mês no eixo X e na tabela: `03/2025` (RNF-ANA-001). */
export function rotuloDoMes(mes: string): string {
  const [ano, numero] = mes.split('-');
  return `${numero}/${ano}`;
}

/**
 * Cores das linhas, na ordem fixa da paleta categórica validada para daltonismo (dataviz, slots 1 a 8). A cor segue
 * a categoria pela posição do id dela entre os ids do catálogo, e não pela posição na seleção, para não mudar quando
 * o filtro muda.
 */
export const CORES_DAS_CATEGORIAS = [
  '#2a78d6',
  '#eb6834',
  '#1baf7a',
  '#eda100',
  '#e87ba4',
  '#008300',
  '#6250d6',
  '#e34948',
] as const;

export function corDaCategoria(id: number, catalogo: readonly number[]): string {
  const posicao = [...catalogo].sort((a, b) => a - b).indexOf(id);
  return CORES_DAS_CATEGORIAS[(posicao < 0 ? 0 : posicao) % CORES_DAS_CATEGORIAS.length] ?? CORES_DAS_CATEGORIAS[0];
}
