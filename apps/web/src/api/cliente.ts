import createClient, { type Middleware } from 'openapi-fetch';
import type { paths } from './esquema';

/** Violação de regra de negócio devolvida no 422 (DEC-07). */
export interface Violacao {
  codigo: string;
  mensagem: string;
}

/** Corpo de erro no formato ProblemDetail (RFC 9457), usado pela API (DEC-07). */
export interface ProblemaDetalhe {
  status: number;
  detail: string;
  title?: string;
  violacoes?: Violacao[];
}

/** Erro de uma resposta da API, com o ProblemDetail já interpretado. */
export class ErroDaApi extends Error {
  readonly problema: ProblemaDetalhe;

  constructor(problema: ProblemaDetalhe) {
    super(problema.detail);
    this.name = 'ErroDaApi';
    this.problema = problema;
  }
}

type TratadorDeNaoAutorizado = () => void;

let tratadorDeNaoAutorizado: TratadorDeNaoAutorizado | null = null;

/**
 * Registra o que fazer quando uma requisição autenticada recebe 401 (limpar a sessão
 * e voltar ao login). A sessão em si fica fora deste módulo para não acoplar o
 * cliente HTTP ao contexto de UI. O retorno remove o registro.
 */
export function definirTratadorDeNaoAutorizado(
  tratador: TratadorDeNaoAutorizado | null,
): () => void {
  tratadorDeNaoAutorizado = tratador;
  return () => {
    if (tratadorDeNaoAutorizado === tratador) {
      tratadorDeNaoAutorizado = null;
    }
  };
}

function lerCookie(nome: string): string | null {
  const prefixo = `${nome}=`;
  for (const parte of document.cookie.split(';')) {
    const item = parte.trim();
    if (item.startsWith(prefixo)) {
      const valor = item.slice(prefixo.length);
      try {
        return decodeURIComponent(valor);
      } catch {
        return valor;
      }
    }
  }
  return null;
}

/** GET e HEAD não precisam do token CSRF; as demais escritas precisam (DEC-08). */
function metodoDeEscrita(metodo: string): boolean {
  const normalizado = metodo.toUpperCase();
  return normalizado !== 'GET' && normalizado !== 'HEAD';
}

function adicionarTokenCsrf(headers: Headers): void {
  const token = lerCookie('XSRF-TOKEN');
  if (token) {
    headers.set('X-XSRF-TOKEN', token);
  }
}

function caminhoDe(url: string): string {
  try {
    return new URL(url, 'http://livraria.local').pathname;
  } catch {
    return url;
  }
}

function ehRotaDeSessao(url: string): boolean {
  return caminhoDe(url) === '/api/sessao';
}

function avisarNaoAutorizado(url: string): void {
  // O 401 do próprio /api/sessao é esperado: significa "ninguém logado" ou
  // "login/senha inválidos", e não deve provocar redirecionamento.
  if (ehRotaDeSessao(url)) {
    return;
  }
  tratadorDeNaoAutorizado?.();
}

const middlewareDeCsrf: Middleware = {
  onRequest({ request }) {
    if (metodoDeEscrita(request.method)) {
      adicionarTokenCsrf(request.headers);
    }
    return request;
  },
  onResponse({ request, response }) {
    if (response.status === 401) {
      avisarNaoAutorizado(request.url);
    }
    return response;
  },
};

/** Cliente tipado pelo contrato do server (DEC-07). URLs relativas à mesma origem. */
export const cliente = createClient<paths>({
  baseUrl: '',
  credentials: 'same-origin',
});

cliente.use(middlewareDeCsrf);

function mensagemPadrao(status: number): string {
  switch (status) {
    case 400:
      return 'Requisição inválida.';
    case 401:
      return 'Sessão expirada. Entre novamente.';
    case 403:
      return 'Você não tem permissão para esta ação.';
    case 404:
      return 'Recurso não encontrado.';
    case 409:
      return 'A operação não pode ser concluída no estado atual.';
    case 422:
      return 'A operação viola regras de negócio.';
    case 500:
      return 'Erro inesperado. Tente novamente.';
    default:
      return 'Não foi possível concluir a operação.';
  }
}

function lerViolacoes(valor: unknown): Violacao[] {
  if (!Array.isArray(valor)) {
    return [];
  }
  const violacoes: Violacao[] = [];
  for (const item of valor) {
    if (typeof item !== 'object' || item === null) {
      continue;
    }
    const registro = item as Record<string, unknown>;
    if (typeof registro.codigo === 'string' && typeof registro.mensagem === 'string') {
      violacoes.push({ codigo: registro.codigo, mensagem: registro.mensagem });
    }
  }
  return violacoes;
}

/**
 * Interpreta o corpo de erro já lido pelo cliente tipado (o `error` do
 * openapi-fetch), com fallback em português quando ele não vem no formato
 * ProblemDetail esperado (erro de proxy, 500 sem corpo etc.).
 */
export function interpretarProblema(corpo: unknown, statusDaResposta: number): ProblemaDetalhe {
  const registro =
    typeof corpo === 'object' && corpo !== null ? (corpo as Record<string, unknown>) : {};
  const status = typeof registro.status === 'number' ? registro.status : statusDaResposta;
  const detalhe = typeof registro.detail === 'string' ? registro.detail.trim() : '';
  const problema: ProblemaDetalhe = {
    status,
    detail: detalhe !== '' ? detalhe : mensagemPadrao(status),
  };

  if (typeof registro.title === 'string' && registro.title !== '') {
    problema.title = registro.title;
  }

  const violacoes = lerViolacoes(registro.violacoes);
  if (violacoes.length > 0) {
    problema.violacoes = violacoes;
  }

  return problema;
}
