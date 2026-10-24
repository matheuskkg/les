import type { paths } from './api/esquema';

/** Parâmetros de `GET /api/admin/analise`, os mesmos da exportação (RF-ANA-001 a RF-ANA-004, DEC-15.4). */
export type ConsultaDaAnalise = paths['/api/admin/analise']['get']['parameters']['query'];

/** Meses do período padrão: os 12 últimos, contando o atual (RN-ANA-002 aceita de 1 a 24). */
export const MESES_DO_PERIODO_PADRAO = 12;

/**
 * Estado da tela na URL. Meses no formato `AAAA-MM`; sem categoria na URL, todas são analisadas, porque a análise
 * exige ao menos uma (RF-ANA-003).
 */
export interface EstadoDaAnalise {
  inicio: string;
  fim: string;
  categorias: number[];
}

const FORMATO_DO_MES = /^\d{4}-(0[1-9]|1[0-2])$/;

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

/** Lê o estado da query string; mês ausente ou malformado volta ao período padrão, que termina em `hoje`. */
export function lerEstadoDaAnalise(busca: string, hoje: Date): EstadoDaAnalise {
  const parametros = new URLSearchParams(busca);
  const fimPadrao = mesDe(hoje);
  const fim = lerMes(parametros.get('fim')) ?? fimPadrao;
  const inicio = lerMes(parametros.get('inicio')) ?? somarMeses(fim, 1 - MESES_DO_PERIODO_PADRAO);
  const categorias = parametros
    .getAll('categoria')
    .map((valor) => Number(valor.trim()))
    .filter((id, indice, todos) => Number.isSafeInteger(id) && id > 0 && todos.indexOf(id) === indice);
  return { inicio, fim, categorias };
}

function lerMes(valor: string | null): string | null {
  const texto = valor?.trim() ?? '';
  return FORMATO_DO_MES.test(texto) ? texto : null;
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
