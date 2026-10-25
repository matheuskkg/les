import type { AnchorHTMLAttributes, ButtonHTMLAttributes } from 'preact';
import { classes } from './classes';

export type VarianteDeBotao = 'primario' | 'secundario' | 'perigo' | 'discreto';

function classeDoBotao(variante: VarianteDeBotao, extra?: string): string {
  return classes('botao', variante !== 'primario' && `botao--${variante}`, extra);
}

type PropsDoBotao = Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'class'> & {
  variante?: VarianteDeBotao;
  class?: string;
};

/** Botão de ação; `type` padrão `button`, para não enviar formulários por engano. */
export function Botao({ variante = 'primario', class: extra, type = 'button', ...resto }: PropsDoBotao) {
  return <button {...resto} type={type} class={classeDoBotao(variante, extra)} />;
}

type PropsDoLink = Omit<AnchorHTMLAttributes<HTMLAnchorElement>, 'class' | 'href' | 'role'> & {
  variante?: VarianteDeBotao;
  class?: string;
  /** Sem destino, aparece desabilitado e não navega. */
  href: string | null;
};

/** Link com aparência de botão (navegação, não ação). */
export function BotaoLink({ variante = 'primario', class: extra, href, children, ...resto }: PropsDoLink) {
  if (href === null) {
    return (
      <span class={classeDoBotao(variante, classes('botao--inativo', extra))} aria-disabled="true">
        {children}
      </span>
    );
  }
  return (
    <a {...resto} href={href} class={classeDoBotao(variante, extra)}>
      {children}
    </a>
  );
}
