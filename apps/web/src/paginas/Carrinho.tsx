import { useCallback, useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { Erro } from '../componentes/Erro';
import { Violacoes } from '../componentes/Violacoes';
import { useRecargaPorNotificacao } from '../contextoDeNotificacoes';
import { formatarDataHora, formatarDinheiro } from '../formatacao';

type CarrinhoDto = components['schemas']['CarrinhoDto'];
type Item = components['schemas']['ItemDoCarrinhoDto'];

function LinhaDoCarrinho({
  item,
  alterar,
  remover,
}: {
  item: Item;
  alterar: (livroId: number, quantidade: number) => void;
  remover: (livroId: number) => void;
}) {
  const [quantidade, definirQuantidade] = useState(String(item.quantidade));

  useEffect(() => definirQuantidade(String(item.quantidade)), [item.quantidade]);

  function aplicar() {
    const unidades = Number.parseInt(quantidade, 10);
    if (Number.isSafeInteger(unidades) && unidades >= 1 && unidades !== item.quantidade) {
      alterar(item.livroId, unidades);
    } else {
      definirQuantidade(String(item.quantidade));
    }
  }

  return (
    <li class="item-da-conta">
      <div>
        <a href={`/livros/${item.livroId}`}>
          <strong>{item.titulo}</strong>
        </a>
        <br />
        {item.autores.join('; ')} · {formatarDinheiro(item.precoUnitario)} cada
      </div>
      <div class="item-da-conta__acoes">
        <label class="adicionar-ao-carrinho__quantidade">
          Quantidade
          <input
            class="campo__entrada"
            type="number"
            min={1}
            value={quantidade}
            onInput={(evento) => definirQuantidade(evento.currentTarget.value)}
            onBlur={aplicar}
            onKeyDown={(evento) => {
              if (evento.key === 'Enter') {
                evento.preventDefault();
                aplicar();
              }
            }}
          />
        </label>
        <strong>{formatarDinheiro(item.subtotal)}</strong>
        <button class="botao botao--secundario" type="button" onClick={() => remover(item.livroId)}>
          Remover
        </button>
      </div>
    </li>
  );
}

/**
 * Carrinho do cliente: itens com o preço atual, alteração de quantidade, remoção e subtotal (RF-VEN-001,
 * RF-VEN-002). O prazo do bloqueio é só exibido (DEC-13); a tela recarrega quando chega uma notificação do carrinho.
 */
export default function Carrinho() {
  const [carrinho, definirCarrinho] = useState<CarrinhoDto | null>(null);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);

  const carregar = useCallback(async () => {
    const { data, error, response } = await cliente.GET('/api/me/carrinho');
    if (!data) {
      definirProblema(interpretarProblema(error, response.status));
      return;
    }
    definirCarrinho(data);
  }, []);

  useEffect(() => {
    void carregar().catch(() =>
      definirProblema({ status: 0, detail: 'Não foi possível carregar o carrinho. Tente novamente.' }),
    );
  }, [carregar]);

  useRecargaPorNotificacao('carrinho', () => void carregar().catch(() => undefined));

  async function aplicar(acao: Promise<{ data?: CarrinhoDto; error?: unknown; response: Response }>) {
    definirProblema(null);
    const { data, error, response } = await acao;
    if (!data) {
      definirProblema(interpretarProblema(error, response.status));
      await carregar();
      return;
    }
    definirCarrinho(data);
  }

  function alterar(livroId: number, quantidade: number) {
    void aplicar(
      cliente.PUT('/api/me/carrinho/itens/{livroId}', { params: { path: { livroId } }, body: { quantidade } }),
    );
  }

  function remover(livroId: number) {
    void aplicar(cliente.DELETE('/api/me/carrinho/itens/{livroId}', { params: { path: { livroId } } }));
  }

  return (
    <section class="pagina">
      <h1>Carrinho</h1>
      {problema?.violacoes && problema.violacoes.length > 0 ? (
        <Violacoes violacoes={problema.violacoes} />
      ) : (
        <Erro problema={problema} />
      )}

      {carrinho === null ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : carrinho.itens.length === 0 ? (
        <div class="cartao">
          <p>Seu carrinho está vazio.</p>
          <a class="botao" href="/">
            Ver livros
          </a>
        </div>
      ) : (
        <div class="cartao">
          <ul class="itens-da-conta">
            {carrinho.itens.map((item) => (
              <LinhaDoCarrinho key={item.livroId} item={item} alterar={alterar} remover={remover} />
            ))}
          </ul>
          <p class="carrinho__total">
            Subtotal ({carrinho.quantidadeDeItens} {carrinho.quantidadeDeItens === 1 ? 'item' : 'itens'}):{' '}
            <strong>{formatarDinheiro(carrinho.subtotal)}</strong>
          </p>
          {carrinho.expiraEm && (
            <p class="campo__ajuda">
              Os itens ficam reservados até {formatarDataHora(carrinho.expiraEm)}. Incluir um livro renova o prazo.
            </p>
          )}
        </div>
      )}
    </section>
  );
}
