import { abrirOpcoesPeloRotulo } from './abrirOpcoes';
import { BotaoLink } from './Botao';

interface OpcaoDeTamanho {
  valor: number;
  rotulo: string;
}

interface Props {
  /** Prefixo dos ids dos seletores, único na página, para os rótulos acessíveis. */
  id: string;
  /** Página atual, a partir de 1. */
  pagina: number;
  totalPaginas: number;
  /** Nome da navegação para leitores de tela, como "Páginas de clientes". */
  rotulo: string;
  /** Endereço de uma página, para que o link possa ser aberto em outra aba e o voltar funcione. */
  urlDaPagina: (pagina: number) => string;
  /** Navegação interna ao escolher página ou tamanho no seletor (o `<select>` não é um link). */
  aoNavegar: (url: string) => void;
  /** Tamanho atual da página; com as opções e a url, mostra o seletor de itens por página. */
  tamanho?: number;
  opcoesDeTamanho?: readonly OpcaoDeTamanho[];
  urlDoTamanho?: (tamanho: number) => string;
}

/**
 * Navegação de uma listagem paginada: Anterior/Próxima, seletor da página desejada e, quando informado, seletor de
 * itens por página. Some só quando não há nada para navegar; o seletor de tamanho continua visível mesmo com uma
 * página só, para que o usuário possa voltar ao tamanho anterior.
 */
export function Paginacao({
  id,
  pagina,
  totalPaginas,
  rotulo,
  urlDaPagina,
  aoNavegar,
  tamanho,
  opcoesDeTamanho,
  urlDoTamanho,
}: Props) {
  const seletorDeTamanho =
    tamanho !== undefined && opcoesDeTamanho && urlDoTamanho
      ? { tamanho, opcoes: opcoesDeTamanho, url: urlDoTamanho }
      : null;
  if (totalPaginas <= 1 && seletorDeTamanho === null) {
    return null;
  }

  const idPagina = `${id}-pagina`;
  const idTamanho = `${id}-tamanho`;
  const paginas = Array.from({ length: Math.max(totalPaginas, 1) }, (_, indice) => indice + 1);

  return (
    <nav class="paginacao" aria-label={rotulo}>
      {seletorDeTamanho && (
        <div class="paginacao__campo">
          <label for={idTamanho} onClick={(evento) => abrirOpcoesPeloRotulo(evento, idTamanho)}>
            Itens por página
          </label>
          <select
            id={idTamanho}
            class="campo__entrada campo__entrada--compacta"
            value={seletorDeTamanho.tamanho}
            onChange={(evento) => aoNavegar(seletorDeTamanho.url(Number(evento.currentTarget.value)))}
          >
            {seletorDeTamanho.opcoes.map((opcao) => (
              <option key={opcao.valor} value={opcao.valor}>
                {opcao.rotulo}
              </option>
            ))}
          </select>
        </div>
      )}
      {totalPaginas > 1 && (
        <>
          <BotaoLink variante="secundario" href={pagina > 1 ? urlDaPagina(pagina - 1) : null}>
            Anterior
          </BotaoLink>
          <div class="paginacao__campo">
            <label for={idPagina} onClick={(evento) => abrirOpcoesPeloRotulo(evento, idPagina)}>
              Página
            </label>
            <select
              id={idPagina}
              class="campo__entrada campo__entrada--compacta"
              value={pagina}
              onChange={(evento) => aoNavegar(urlDaPagina(Number(evento.currentTarget.value)))}
            >
              {paginas.map((numero) => (
                <option key={numero} value={numero}>
                  {numero}
                </option>
              ))}
            </select>
            <span class="paginacao__posicao">de {totalPaginas}</span>
          </div>
          <BotaoLink variante="secundario" href={pagina < totalPaginas ? urlDaPagina(pagina + 1) : null}>
            Próxima
          </BotaoLink>
        </>
      )}
    </nav>
  );
}
