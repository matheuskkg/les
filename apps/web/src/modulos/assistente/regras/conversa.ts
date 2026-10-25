import type { components } from '../../../api/esquema';

export type Mensagem = components['schemas']['MensagemDto'];
export type AvaliacaoDaResposta = NonNullable<Mensagem['avaliacao']>;

/** Limite da mensagem do cliente, igual ao do server (RNF-REC-001, DEC-03). */
export const TAMANHO_MAXIMO = 500;

/** Perguntas de exemplo da conversa vazia; clicar envia a pergunta. */
export const SUGESTOES: readonly string[] = [
  'Quero uma fantasia com dragões e aventura',
  'Tem livros de ficção científica clássica?',
  'Me indique algo parecido com o que já comprei',
];

/** Caracteres como o server conta: por code point, então um emoji vale um. */
export function contarCaracteres(texto: string): number {
  return [...texto].length;
}

/** Envio liberado com texto, dentro do limite e sem outra mensagem aguardando resposta. */
export function podeEnviar(texto: string, aguardando: boolean): boolean {
  const tamanho = contarCaracteres(texto.trim());
  return !aguardando && tamanho > 0 && tamanho <= TAMANHO_MAXIMO;
}

/** Enter envia; Shift+Enter quebra a linha; durante a composição (acentos, IME) a tecla não envia. */
export function enviaAoTeclar(tecla: { key: string; shiftKey: boolean; isComposing: boolean }): boolean {
  return tecla.key === 'Enter' && !tecla.shiftKey && !tecla.isComposing;
}

/** Mensagem do cliente exibida na hora, antes da resposta; o id negativo nunca colide com os do server. */
export function mensagemProvisoria(texto: string, id: number, agora: Date): Mensagem {
  return { id: -id, papel: 'CLIENTE', texto: texto.trim(), livros: [], enviadaEm: agora.toISOString() };
}

/** A avaliação de uma resposta: a última vale (DEC-03). */
export function aplicarAvaliacao(
  mensagens: readonly Mensagem[],
  id: number,
  avaliacao: AvaliacaoDaResposta,
): Mensagem[] {
  return mensagens.map((mensagem) => (mensagem.id === id ? { ...mensagem, avaliacao } : mensagem));
}

/** O widget fica em toda a loja, fora das telas do administrador (DEC-03). */
export function assistenteNaRota(caminho: string): boolean {
  return caminho !== '/admin' && !caminho.startsWith('/admin/');
}

/**
 * Destino da volta depois do login pelo convite do visitante: a página atual, exceto o próprio login e o cadastro,
 * que voltariam para eles mesmos.
 */
export function urlDoConvite(caminho: string, url: string): string {
  if (caminho === '/login' || caminho === '/cadastro') {
    return '/login';
  }
  return `/login?destino=${encodeURIComponent(url)}`;
}

/** Estado aberto/fechado guardado na aba; qualquer valor diferente de "1" é fechado. */
export function lerAberto(valor: string | null): boolean {
  return valor === '1';
}
