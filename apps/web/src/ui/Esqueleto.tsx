interface Props {
  /** Quantidade de linhas de texto simuladas. */
  linhas?: number;
  /** Texto lido por leitores de tela enquanto carrega. */
  rotulo?: string;
  variante?: 'texto' | 'grade';
}

/** Marcador de carregamento com o formato do conteúdo, no lugar da tela em branco. */
export function Esqueleto({ linhas = 3, rotulo = 'Carregando…', variante = 'texto' }: Props) {
  return (
    <div class={`esqueleto esqueleto--${variante}`} role="status" aria-live="polite">
      <span class="visualmente-oculto">{rotulo}</span>
      {Array.from({ length: variante === 'grade' ? 8 : linhas }, (_, indice) => (
        <span class="esqueleto__bloco" key={indice} aria-hidden="true" />
      ))}
    </div>
  );
}
