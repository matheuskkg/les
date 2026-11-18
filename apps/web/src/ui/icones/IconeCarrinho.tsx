import { IconeBase, type PropsDoIcone } from './IconeBase';

/** Ícone do carrinho usado pelo botão da gaveta no cabeçalho (RF-VEN-001, ALT-12/ALT-15). */
export function IconeCarrinho(props: PropsDoIcone) {
  return (
    <IconeBase {...props}>
      <circle cx="9" cy="20" r="1" />
      <circle cx="18" cy="20" r="1" />
      <path d="M2 3h2.5l2.4 11.2a2 2 0 0 0 2 1.6h8.8a2 2 0 0 0 2-1.6L21 7H5.2" />
    </IconeBase>
  );
}
