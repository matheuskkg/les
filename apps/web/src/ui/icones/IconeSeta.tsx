import { IconeBase, type PropsDoIcone } from './IconeBase';

/**
 * Chevron para baixo, usado no gatilho do menu do usuário (ALT-16).
 * Para apontar para cima quando o menu abre, gire por CSS na classe do chamador.
 */
export function IconeSeta(props: PropsDoIcone) {
  return (
    <IconeBase {...props}>
      <path d="m6 9 6 6 6-6" />
    </IconeBase>
  );
}
