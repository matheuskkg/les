import type { ComponentChildren } from 'preact';

interface Props {
  numero: number;
  titulo: string;
  /** Resumo do que já foi escolhido, ao lado do título. */
  resumo?: string | null;
  concluido?: boolean;
  children: ComponentChildren;
}

/** Um passo numerado do checkout; todos ficam na tela para o que foi digitado não se perder ao voltar. */
export function PassoDaCompra({ numero, titulo, resumo, concluido, children }: Props) {
  const id = `passo-${numero}`;
  return (
    <section class={`cartao passo${concluido ? ' passo--concluido' : ''}`} id={id} aria-labelledby={`${id}-titulo`}>
      <header class="passo__topo">
        <span class="passo__numero" aria-hidden="true">
          {concluido ? '✓' : numero}
        </span>
        <h2 id={`${id}-titulo`}>{titulo}</h2>
        {resumo && <span class="passo__resumo">{resumo}</span>}
      </header>
      {children}
    </section>
  );
}

/** Indicador dos passos no topo, com link para cada um. */
export function IndicadorDePassos({ passos }: { passos: readonly { titulo: string; concluido: boolean }[] }) {
  const atual = passos.findIndex((passo) => !passo.concluido);
  return (
    <ol class="indicador-de-passos" aria-label="Passos da compra">
      {passos.map((passo, indice) => (
        <li
          key={passo.titulo}
          class={passo.concluido ? 'indicador-de-passos__item--concluido' : indice === atual ? 'indicador-de-passos__item--atual' : ''}
          aria-current={indice === atual ? 'step' : undefined}
        >
          <a href={`#passo-${indice + 1}`}>
            <span aria-hidden="true">{passo.concluido ? '✓' : indice + 1}</span> {passo.titulo}
          </a>
        </li>
      ))}
    </ol>
  );
}
