import type { ComponentChildren } from 'preact';
import { classes } from './classes';

interface Props {
  titulo?: ComponentChildren;
  /** Ações à direita do título. */
  acoes?: ComponentChildren;
  class?: string;
  id?: string;
  rotulo?: string;
  como?: 'section' | 'div' | 'article' | 'aside';
  children?: ComponentChildren;
}

/** Superfície com borda que agrupa um assunto da tela. */
export function Painel({ titulo, acoes, class: extra, id, rotulo, como: Elemento = 'section', children }: Props) {
  return (
    <Elemento id={id} aria-label={rotulo} class={classes('cartao', extra)}>
      {(titulo !== undefined || acoes !== undefined) && (
        <header class="cartao__topo">
          {titulo !== undefined && <h2 class="cartao__titulo">{titulo}</h2>}
          {acoes !== undefined && <div class="cartao__acoes">{acoes}</div>}
        </header>
      )}
      {children}
    </Elemento>
  );
}
