import { IconeBase, type PropsDoIcone } from './IconeBase';

/** Balão de conversa, usado no botão flutuante do assistente da livraria (RNF-REC-001, ALT-01). */
export function IconeBalao(props: PropsDoIcone) {
  return (
    <IconeBase {...props}>
      <path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z" />
    </IconeBase>
  );
}
