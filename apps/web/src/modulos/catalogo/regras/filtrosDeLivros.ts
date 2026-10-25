import type { paths } from '../../../api/esquema';

/** Parâmetros de `GET /api/livros`, como definidos no contrato (DEC-07). */
export type ConsultaDeLivros = NonNullable<paths['/api/livros']['get']['parameters']['query']>;

/**
 * Campos de filtro da vitrine (RF-LIV-005), guardados como texto: é o que vem do
 * formulário e da URL. A conversão para os tipos do contrato fica em `consultaDoEstado`.
 */
export interface FiltrosDeLivros {
  /** Busca livre: título, autor, editora ou ISBN. */
  q: string;
  titulo: string;
  autor: string;
  editora: string;
  isbn: string;
  codigo: string;
  anoDe: string;
  anoAte: string;
  categorias: string[];
}

/** O que a vitrine mostra: filtros do formulário, ordenação e página (a partir de 1 na URL). */
export interface EstadoDaVitrine {
  filtros: FiltrosDeLivros;
  ordem: string;
  pagina: number;
}

export interface OrdemDeLivros {
  valor: string;
  rotulo: string;
}

export const TAMANHO_DA_PAGINA = 20;
export const PAGINA_PADRAO = 1;
export const ORDEM_PADRAO = 'titulo,asc';

export const ORDENS: readonly OrdemDeLivros[] = [
  { valor: 'titulo,asc', rotulo: 'Título A–Z' },
  { valor: 'titulo,desc', rotulo: 'Título Z–A' },
  { valor: 'ano,desc', rotulo: 'Ano' },
  { valor: 'editora,asc', rotulo: 'Editora' },
];

const CAMPOS_DE_TEXTO = [
  'q',
  'titulo',
  'autor',
  'editora',
  'isbn',
  'codigo',
  'anoDe',
  'anoAte',
] as const;

export type CampoDeTexto = (typeof CAMPOS_DE_TEXTO)[number];

export function filtrosVazios(): FiltrosDeLivros {
  return {
    q: '',
    titulo: '',
    autor: '',
    editora: '',
    isbn: '',
    codigo: '',
    anoDe: '',
    anoAte: '',
    categorias: [],
  };
}

/** Ordenação do contrato, ou o padrão quando a URL traz um valor desconhecido. */
export function ordemValida(valor: string | null): string {
  if (valor !== null && ORDENS.some((ordem) => ordem.valor === valor)) {
    return valor;
  }
  return ORDEM_PADRAO;
}

function inteiroNaoNegativo(valor: string): number | undefined {
  const texto = valor.trim();
  if (!/^\d+$/.test(texto)) {
    return undefined;
  }
  const numero = Number(texto);
  return Number.isSafeInteger(numero) ? numero : undefined;
}

/**
 * Lê o estado da vitrine da query string (com ou sem "?"). Parâmetros
 * desconhecidos ou inválidos caem no padrão, sem quebrar a página.
 */
export function lerEstadoDaVitrine(busca: string): EstadoDaVitrine {
  const parametros = new URLSearchParams(busca);
  const filtros = filtrosVazios();

  for (const campo of CAMPOS_DE_TEXTO) {
    filtros[campo] = (parametros.get(campo) ?? '').trim();
  }

  const categorias = new Set<string>();
  for (const categoria of parametros.getAll('categoria')) {
    if (/^\d+$/.test(categoria.trim())) {
      categorias.add(categoria.trim());
    }
  }
  filtros.categorias = [...categorias];

  const pagina = Number.parseInt(parametros.get('pagina') ?? '', 10);

  return {
    filtros,
    ordem: ordemValida(parametros.get('ordem')),
    pagina: Number.isSafeInteger(pagina) && pagina > PAGINA_PADRAO ? pagina : PAGINA_PADRAO,
  };
}

/**
 * Query string do estado, com os padrões omitidos para manter o link curto.
 * Devolve "" quando não há nada a informar.
 */
export function buscaDoEstado(estado: EstadoDaVitrine): string {
  const parametros = new URLSearchParams();

  for (const campo of CAMPOS_DE_TEXTO) {
    const valor = estado.filtros[campo].trim();
    if (valor !== '') {
      parametros.append(campo, valor);
    }
  }

  for (const categoria of estado.filtros.categorias) {
    const valor = categoria.trim();
    if (valor !== '') {
      parametros.append('categoria', valor);
    }
  }

  if (estado.ordem !== ORDEM_PADRAO) {
    parametros.set('ordem', estado.ordem);
  }
  if (estado.pagina > PAGINA_PADRAO) {
    parametros.set('pagina', String(estado.pagina));
  }

  const texto = parametros.toString();
  return texto === '' ? '' : `?${texto}`;
}

/** Caminho da vitrine para o estado, pronto para `href` e para `route()`. */
/** Quantos filtros estão em uso, para o resumo de "Mais filtros" e o botão de limpar. */
export function filtrosEmUso(filtros: FiltrosDeLivros): number {
  const textos = CAMPOS_DE_TEXTO.filter((campo) => filtros[campo].trim() !== '').length;
  return textos + filtros.categorias.length;
}

export function urlDoEstado(estado: EstadoDaVitrine): string {
  return `/${buscaDoEstado(estado)}`;
}

/**
 * Traduz o estado da tela na consulta do contrato (RF-LIV-005, DEC-07):
 * texto vazio é omitido, números só entram quando são válidos e a página vai
 * a partir de 0. Intervalos invertidos são enviados de propósito, para que o
 * servidor responda 400 com o detalhe da validação.
 */
export function consultaDoEstado(estado: EstadoDaVitrine): ConsultaDeLivros {
  const consulta: ConsultaDeLivros = {
    pagina: Math.max(estado.pagina - PAGINA_PADRAO, 0),
    tamanho: TAMANHO_DA_PAGINA,
    ordem: [estado.ordem],
  };

  for (const campo of ['q', 'titulo', 'autor', 'editora', 'isbn'] as const) {
    const valor = estado.filtros[campo].trim();
    if (valor !== '') {
      consulta[campo] = valor;
    }
  }

  const codigo = inteiroNaoNegativo(estado.filtros.codigo);
  if (codigo !== undefined) {
    consulta.codigo = codigo;
  }
  const anoDe = inteiroNaoNegativo(estado.filtros.anoDe);
  if (anoDe !== undefined) {
    consulta.anoDe = anoDe;
  }
  const anoAte = inteiroNaoNegativo(estado.filtros.anoAte);
  if (anoAte !== undefined) {
    consulta.anoAte = anoAte;
  }

  const categorias: number[] = [];
  for (const categoria of estado.filtros.categorias) {
    const valor = inteiroNaoNegativo(categoria);
    if (valor !== undefined) {
      categorias.push(valor);
    }
  }
  if (categorias.length > 0) {
    consulta.categoria = categorias;
  }

  return consulta;
}
