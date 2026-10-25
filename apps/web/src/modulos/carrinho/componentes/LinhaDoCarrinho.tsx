import type { components } from '../../../api/esquema';
import { formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';
import { CapaDeLivro } from '../../../ui/CapaDeLivro';
import { SeletorDeQuantidade } from '../../../ui/SeletorDeQuantidade';

type Item = components['schemas']['ItemComPrecoDto'];

interface Props {
  item: Item;
  alterar: (livroId: number, quantidade: number) => void;
  remover: (livroId: number) => void;
  /** Na gaveta a linha é mais estreita. */
  compacta?: boolean;
}

/** Livro no carrinho com o preço atual, a quantidade e o subtotal (RF-VEN-001, RF-VEN-002). */
export function LinhaDoCarrinho({ item, alterar, remover, compacta = false }: Props) {
  const titulo = item.titulo ?? `Livro ${item.livroId}`;
  return (
    <li class={compacta ? 'item-da-conta item-do-carrinho item-do-carrinho--compacto' : 'item-da-conta item-do-carrinho'}>
      <a href={`/livros/${item.livroId}`} class="item-do-carrinho__capa" tabIndex={-1}>
        <CapaDeLivro titulo={titulo} autores={item.autores} tamanho="pequena" />
      </a>
      <div class="item-do-carrinho__texto">
        <a href={`/livros/${item.livroId}`}>
          <strong>{titulo}</strong>
        </a>
        <span class="item-do-carrinho__detalhe">
          {item.titulo === undefined ? 'Livro indisponível' : item.autores.join('; ')} ·{' '}
          {item.precoUnitario ? `${formatarDinheiro(item.precoUnitario)} cada` : 'Fora de venda: remova do carrinho'}
        </span>
      </div>
      <div class="item-da-conta__acoes">
        <SeletorDeQuantidade
          id={`quantidade-${compacta ? 'gaveta-' : ''}${item.livroId}`}
          valor={item.quantidade}
          aoMudar={(quantidade) => alterar(item.livroId, quantidade)}
        />
        {item.subtotal && <strong class="item-do-carrinho__subtotal">{formatarDinheiro(item.subtotal)}</strong>}
        <Botao variante="discreto" onClick={() => remover(item.livroId)}>
          Remover
        </Botao>
      </div>
    </li>
  );
}
