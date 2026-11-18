/**
 * Leitura e reconstrução da query string que preserva a consulta ao abrir um detalhe e voltar (RF-CLI-004,
 * RF-VEN-009, RF-VEN-013, RNF-GER-001). Regras puras: o estado da consulta continua na URL, e o detalhe guarda em
 * `de` a URL da tela de origem, sem estado global novo.
 */

const PARAMETRO_DE_ORIGEM = 'de';

/** Base fixa, usada apenas para resolver um destino relativo e conferir que ele continua na mesma origem. */
const BASE_DE_REFERENCIA = 'http://localhost/';

/** A query string de `url`, incluindo `?`, ou vazia quando não há (RF-VEN-009). */
export function buscaDaUrl(url: string): string {
  const indice = url.indexOf('?');
  return indice === -1 ? '' : url.slice(indice);
}

/**
 * Aceita como retorno só um caminho absoluto da mesma origem. A origem é escrita pelo próprio app, mas um `de`
 * alterado na barra de endereço não pode virar redirect externo (RF-VEN-009).
 *
 * Caracteres de controle são recusados antes da resolução: o parser de URL do navegador os remove, então `/\t/evil`
 * vira `//evil`. Depois disso, a origem é conferida com `new URL`, como em `destinoSeguro`.
 */
export function caminhoInterno(destino: string, origem: string = BASE_DE_REFERENCIA): boolean {
  if (!destino.startsWith('/') || destino.includes('\\') || /[\u0000-\u001F\u007F]/.test(destino)) {
    return false;
  }
  try {
    const base = new URL(origem);
    const alvo = new URL(destino, base);
    return alvo.origin === base.origin && alvo.pathname.startsWith('/');
  } catch {
    return false;
  }
}

/**
 * URL do detalhe com a origem codificada em `de`, para o "Voltar" reconstruir a consulta (RF-CLI-004, RF-VEN-009).
 */
export function urlDoDetalhe(caminhoDoDetalhe: string, urlDeVolta: string): string {
  return `${caminhoDoDetalhe}?${PARAMETRO_DE_ORIGEM}=${encodeURIComponent(urlDeVolta)}`;
}

/**
 * URL da origem lida de `de`, ou `caminhoPadrao` quando não há origem válida (RF-VEN-009, RF-VEN-013).
 */
export function urlDeVolta(busca: string, caminhoPadrao: string): string {
  const origem = new URLSearchParams(busca).get(PARAMETRO_DE_ORIGEM);
  return origem !== null && caminhoInterno(origem) ? origem : caminhoPadrao;
}
