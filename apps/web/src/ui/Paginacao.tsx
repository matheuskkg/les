import { BotaoLink } from './Botao';

interface Props {
  /** Página atual, a partir de 1. */
  pagina: number;
  totalPaginas: number;
  /** Nome da navegação para leitores de tela, como "Páginas de clientes". */
  rotulo: string;
  /** Endereço de uma página, para que o link possa ser aberto em outra aba e o voltar funcione. */
  urlDaPagina: (pagina: number) => string;
}

/** Navegação Anterior / Próxima de uma listagem paginada; some quando há uma página só. */
export function Paginacao({ pagina, totalPaginas, rotulo, urlDaPagina }: Props) {
  if (totalPaginas <= 1) {
    return null;
  }
  return (
    <nav class="paginacao" aria-label={rotulo}>
      <BotaoLink variante="secundario" href={pagina > 1 ? urlDaPagina(pagina - 1) : null}>
        Anterior
      </BotaoLink>
      <span class="paginacao__posicao">
        Página {pagina} de {totalPaginas}
      </span>
      <BotaoLink variante="secundario" href={pagina < totalPaginas ? urlDaPagina(pagina + 1) : null}>
        Próxima
      </BotaoLink>
    </nav>
  );
}
