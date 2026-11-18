import { IconeBase, type PropsDoIcone } from './IconeBase';

/** Ícone de fechar (X), usado no botão que fecha o painel do assistente (ALT-17). */
export function IconeFechar(props: PropsDoIcone) {
  return (
    <IconeBase {...props}>
      <path d="M18 6 6 18" />
      <path d="m6 6 12 12" />
    </IconeBase>
  );
}
