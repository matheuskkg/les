interface Props {
  texto: string;
  link?: { href: string; rotulo: string };
}

/** Confirmação passageira de uma ação, no rodapé da tela e anunciada por leitores de tela. */
export function AvisoFlutuante({ texto, link }: Props) {
  return (
    <p class="aviso-flutuante" role="status">
      {texto}
      {link && <a href={link.href}>{link.rotulo}</a>}
    </p>
  );
}
