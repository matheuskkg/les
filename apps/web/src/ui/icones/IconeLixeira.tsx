import { IconeBase, type PropsDoIcone } from './IconeBase';

/** Lixeira, usada no botão "Limpar conversa" do cabeçalho do painel do assistente (RNF-REC-001, ALT-01). */
export function IconeLixeira(props: PropsDoIcone) {
  return (
    <IconeBase {...props}>
      <path d="M3 6h18" />
      <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6" />
      <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" />
      <line x1="10" x2="10" y1="11" y2="17" />
      <line x1="14" x2="14" y1="11" y2="17" />
    </IconeBase>
  );
}
