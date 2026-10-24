import { useCallback, useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { Erro } from '../componentes/Erro';
import { Violacoes } from '../componentes/Violacoes';
import { useRecargaPorNotificacao } from '../contextoDeNotificacoes';
import { formatarDataHora, formatarDinheiro } from '../formatacao';

type CarrinhoDto = components['schemas']['CarrinhoDto'];
type Item = components['schemas']['ItemDoCarrinhoDto'];
type Removido = components['schemas']['ItemRemovidoDoCarrinhoDto'];

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
          <strong>{item.titulo ?? `Livro ${item.livroId}`}</strong>
        </a>
        <br />
        {item.titulo === undefined ? 'Livro indisponível' : item.autores.join('; ')} ·{' '}
        {item.precoUnitario ? `${formatarDinheiro(item.precoUnitario)} cada` : 'Fora de venda: remova do carrinho'}
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
        {item.subtotal && <strong>{formatarDinheiro(item.subtotal)}</strong>}
        <button class="botao botao--secundario" type="button" onClick={() => remover(item.livroId)}>
          Remover
        </button>
      </div>
    </li>
  );
}

/**
 * Livros retirados do carrinho porque o prazo de reserva acabou (RNF-VEN-001): sem opção de compra até serem
 * adicionados de novo, o que apaga o aviso, assim como dispensá-lo.
 */
function ItensRemovidos({
  removidos,
  readicionar,
  dispensar,
}: {
  removidos: Removido[];
  readicionar: (removido: Removido) => void;
  dispensar: (livroId: number) => void;
}) {
  return (
    <div class="cartao">
      <h2>Removidos por expiração</h2>
      <p class="campo__ajuda">
        O prazo de reserva acabou e estes livros saíram do carrinho. Para comprá-los, adicione-os de novo.
      </p>
      <ul class="itens-da-conta">
        {removidos.map((removido) => (
          <li key={removido.livroId} class="item-da-conta item-removido">
            <div>
              <a href={`/livros/${removido.livroId}`}>
                <strong>{removido.titulo ?? `Livro ${removido.livroId}`}</strong>
              </a>
              <br />
              {removido.quantidade} {removido.quantidade === 1 ? 'unidade' : 'unidades'} · removido em{' '}
              {formatarDataHora(removido.removidoEm)}
            </div>
            <div class="item-da-conta__acoes">
              <button class="botao" type="button" disabled title="Adicione o livro de novo para comprá-lo">
                Comprar
              </button>
              <button class="botao botao--secundario" type="button" onClick={() => readicionar(removido)}>
                Adicionar de novo
              </button>
              <button class="botao botao--secundario" type="button" onClick={() => dispensar(removido.livroId)}>
                Dispensar
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

/**
 * Carrinho do cliente: itens com o preço atual, alteração de quantidade, remoção e subtotal (RF-VEN-001,
 * RF-VEN-002), além dos livros retirados por expiração (RN-VEN-014, RNF-VEN-001). O prazo do bloqueio é só exibido
 * (DEC-13); a tela recarrega quando chega uma notificação do carrinho.
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

  function readicionar(removido: Removido) {
    void aplicar(
      cliente.POST('/api/me/carrinho/itens', {
        body: { livroId: removido.livroId, quantidade: removido.quantidade },
      }),
    );
  }

  function dispensar(livroId: number) {
    void aplicar(cliente.DELETE('/api/me/carrinho/removidos/{livroId}', { params: { path: { livroId } } }));
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

      {carrinho && carrinho.removidos.length > 0 && (
        <ItensRemovidos removidos={carrinho.removidos} readicionar={readicionar} dispensar={dispensar} />
      )}
    </section>
  );
}
