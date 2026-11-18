import { useEffect, useState } from 'preact/hooks';
import { abrirOpcoesPeloRotulo } from './abrirOpcoes';
import { BotaoLink } from './Botao';
import { apenasDigitos, paginaDaEntrada } from './regras/pagina.ts';

interface OpcaoDeTamanho {
  valor: number;
  rotulo: string;
}

interface Props {
  /** Prefixo dos ids dos controles, único na página, para os rótulos acessíveis. */
  id: string;
  /** Página atual, a partir de 1. */
  pagina: number;
  totalPaginas: number;
  /** Nome da navegação para leitores de tela, como "Páginas de clientes". */
  rotulo: string;
  /** Endereço de uma página, para que o link possa ser aberto em outra aba e o voltar funcione. */
  urlDaPagina: (pagina: number) => string;
  /** Navegação interna ao escolher página ou tamanho (o campo e o `<select>` não são links). */
  aoNavegar: (url: string) => void;
  /** Tamanho atual da página; com as opções e a url, mostra o seletor de itens por página. */
  tamanho?: number;
  opcoesDeTamanho?: readonly OpcaoDeTamanho[];
  urlDoTamanho?: (tamanho: number) => string;
}

/**
 * Navegação de uma listagem paginada: Anterior/Próxima, campo para digitar a página desejada e, quando informado,
 * seletor de itens por página. Some só quando não há nada para navegar; o seletor de tamanho continua visível mesmo com
 * uma página só, para que o usuário possa voltar ao tamanho anterior.
 *
 * O campo de página aceita só dígitos (máscara no evento, inclusive ao colar) e navega ao sair dele ou com Enter,
 * limitando a entrada ao intervalo `1..totalPaginas` (RNF-GER-001).
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

  // Hooks antes de qualquer retorno: uma instância montada pode perder o campo quando o total cai para 1.
  const [textoPagina, definirTextoPagina] = useState(String(pagina));

  useEffect(() => definirTextoPagina(String(pagina)), [pagina]);

  if (totalPaginas <= 1 && seletorDeTamanho === null) {
    return null;
  }

  function aplicarPagina() {
    const destino = paginaDaEntrada(textoPagina, totalPaginas);
    if (destino === null) {
      definirTextoPagina(String(pagina));
      return;
    }
    definirTextoPagina(String(destino));
    if (destino !== pagina) {
      aoNavegar(urlDaPagina(destino));
    }
  }

  const idPagina = `${id}-pagina`;
  const idTamanho = `${id}-tamanho`;
  const idDicaPagina = `${idPagina}-dica`;

  return (
    <nav class="paginacao" aria-label={rotulo}>
      {seletorDeTamanho && (
        <div class="paginacao__campo paginacao__tamanho">
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
        <div class="paginacao__central">
          <BotaoLink variante="secundario" href={pagina > 1 ? urlDaPagina(pagina - 1) : null}>
            Anterior
          </BotaoLink>
          <div class="paginacao__campo">
            {/* O rótulo continua associado ao campo para leitores de tela; só a exibição visual sai da linha. */}
            <label for={idPagina} class="visualmente-oculto">
              Página
            </label>
            <span id={idDicaPagina} class="visualmente-oculto">
              Digite um número de 1 a {totalPaginas} e pressione Enter ou saia do campo para navegar.
            </span>
            <input
              id={idPagina}
              class="campo__entrada campo__entrada--compacta paginacao__entrada-pagina"
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              autocomplete="off"
              aria-describedby={idDicaPagina}
              title={`Informe um número de 1 a ${totalPaginas}. Pressione Enter ou saia do campo para navegar.`}
              value={textoPagina}
              onInput={(evento) => {
                if (evento.isComposing) {
                  return;
                }
                const digitos = apenasDigitos(evento.currentTarget.value);
                // O setState pode não gerar re-render quando o valor sanitizado é igual ao estado; sem escrever no nó,
                // o caractere inválido continua visível no DOM.
                evento.currentTarget.value = digitos;
                definirTextoPagina(digitos);
              }}
              onBlur={aplicarPagina}
              onKeyDown={(evento) => {
                if (evento.key === 'Enter') {
                  evento.preventDefault();
                  evento.currentTarget.blur();
                }
              }}
            />
            <span class="paginacao__posicao">de {totalPaginas}</span>
          </div>
          <BotaoLink variante="secundario" href={pagina < totalPaginas ? urlDaPagina(pagina + 1) : null}>
            Próxima
          </BotaoLink>
        </div>
      )}
    </nav>
  );
}
