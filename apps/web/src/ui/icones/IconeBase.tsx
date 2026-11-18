import type { ComponentChildren } from 'preact';
import { classes } from '../classes';

/** Props compartilhadas por todos os ícones de `ui/icones/`. */
export interface PropsDoIcone {
  /** Classe CSS do `<svg>`; a cor vem de `currentColor` e o tamanho pode vir do CSS do chamador. */
  class?: string;
  /** Alias de `class`, para quem escreve no estilo React. */
  className?: string;
  /** Largura e altura em px (ou unidade CSS, ex.: `'1.2em'`). Sem valor, o tamanho fica a cargo do CSS. */
  tamanho?: number | string;
}

interface Props extends PropsDoIcone {
  children: ComponentChildren;
}

/**
 * Casca comum aos ícones: traço na cor corrente e SVG decorativo por padrão
 * (`aria-hidden`/`focusable`). Quem usa o ícone continua responsável pelo nome
 * acessível do controle, normalmente com `aria-label`.
 */
export function IconeBase({ class: classe, className, tamanho, children }: Props) {
  const dimensao = tamanho === undefined ? undefined : typeof tamanho === 'number' ? `${tamanho}` : tamanho;
  return (
    <svg
      class={classes(classe, className)}
      width={dimensao}
      height={dimensao}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      {children}
    </svg>
  );
}
