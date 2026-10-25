import { classes } from './classes';

/** Pares de cor (claro, escuro) das capas; a categoria escolhe o par, então livros do mesmo gênero combinam. */
const PALETAS: readonly (readonly [string, string])[] = [
  ['#a2475a', '#6e2a39'],
  ['#2f6aa0', '#1d3f63'],
  ['#2f7a58', '#1d4d38'],
  ['#8a6f3a', '#584624'],
  ['#7046a0', '#472c6b'],
  ['#3d7d86', '#244e54'],
  ['#9a5a2c', '#62391b'],
];

function indiceDaPaleta(chave: string): number {
  let soma = 0;
  for (const letra of chave) {
    soma = (soma * 31 + letra.charCodeAt(0)) >>> 0;
  }
  return soma % PALETAS.length;
}

interface Props {
  titulo: string;
  autores: readonly string[];
  /** Define a cor; sem categoria, a cor vem do título. */
  categoria?: string;
  tamanho?: 'pequena' | 'media' | 'grande';
}

/**
 * Capa tipográfica gerada no próprio front, já que o catálogo não guarda imagens: fundo pela categoria,
 * título e autor. É decorativa para leitores de tela, que leem o título no texto ao lado.
 */
export function CapaDeLivro({ titulo, autores, categoria, tamanho = 'media' }: Props) {
  const [clara, escura] = PALETAS[indiceDaPaleta(categoria ?? titulo)] ?? PALETAS[0] ?? ['#2f6b4f', '#1d4532'];
  return (
    <div
      class={classes('capa', `capa--${tamanho}`)}
      style={{ background: `linear-gradient(160deg, ${clara}, ${escura})` }}
      aria-hidden="true"
    >
      <span class="capa__titulo">{titulo}</span>
      {tamanho !== 'pequena' && <span class="capa__autor">{autores.join(', ')}</span>}
    </div>
  );
}
