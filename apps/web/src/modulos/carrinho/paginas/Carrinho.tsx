import { useEffect } from 'preact/hooks';
import type { components } from '../../../api/esquema';
import { formatarDataHora, formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao, BotaoLink } from '../../../ui/Botao';
import { Erro } from '../../../ui/Erro';
import { Esqueleto } from '../../../ui/Esqueleto';
import { Painel } from '../../../ui/Painel';
import { Violacoes } from '../../../ui/Violacoes';
import { LinhaDoCarrinho } from '../componentes/LinhaDoCarrinho';
import { PrazoDaReserva } from '../componentes/PrazoDaReserva';
import { useCarrinho } from '../contextoDoCarrinho';

type Removido = components['schemas']['ItemRemovidoDoCarrinhoDto'];

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
    <Painel titulo="Removidos por expiração">
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
              <Botao disabled title="Adicione o livro de novo para comprá-lo">
                Comprar
              </Botao>
              <Botao variante="secundario" onClick={() => readicionar(removido)}>
                Adicionar de novo
              </Botao>
              <Botao variante="discreto" onClick={() => dispensar(removido.livroId)}>
                Dispensar
              </Botao>
            </div>
          </li>
        ))}
      </ul>
    </Painel>
  );
}

/**
 * Carrinho do cliente: itens com o preço atual, alteração de quantidade, remoção e subtotal (RF-VEN-001,
 * RF-VEN-002), além dos livros retirados por expiração (RN-VEN-014, RNF-VEN-001). O prazo do bloqueio aparece como
 * contagem regressiva; os dados vêm do contexto do carrinho, que recarrega a cada notificação (DEC-13).
 */
export default function Carrinho() {
  const {
    carrinho,
    problema,
    alterar,
    remover,
    adicionar,
    dispensarRemovido,
    irParaPagamento,
    iniciandoPagamento,
    recarregar,
  } = useCarrinho();

  // O checkout pode ter devolvido itens ao carrinho desde a última leitura (RN-VEN-014).
  useEffect(() => {
    void recarregar();
  }, [recarregar]);

  async function readicionar(removido: Removido) {
    await adicionar(removido.livroId, removido.quantidade);
    await recarregar();
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
        <Esqueleto rotulo="Carregando o carrinho…" />
      ) : carrinho.itens.length === 0 ? (
        <Painel>
          <p>Seu carrinho está vazio.</p>
          <BotaoLink href="/">Ver livros</BotaoLink>
        </Painel>
      ) : (
        <div class="carrinho">
          <Painel class="carrinho__itens">
            <ul class="itens-da-conta">
              {carrinho.itens.map((item) => (
                <LinhaDoCarrinho key={item.livroId} item={item} alterar={alterar} remover={remover} />
              ))}
            </ul>
          </Painel>
          <Painel como="aside" class="carrinho__resumo" titulo="Resumo">
            {carrinho.expiraEm && (
              <p class="reserva">
                <PrazoDaReserva expiraEm={carrinho.expiraEm} />
              </p>
            )}
            <p class="linha-de-total linha-de-total--forte carrinho__total">
              <span>
                Subtotal ({carrinho.quantidadeDeItens} {carrinho.quantidadeDeItens === 1 ? 'item' : 'itens'})
              </span>
              <strong>{formatarDinheiro(carrinho.subtotal)}</strong>
            </p>
            <p class="campo__ajuda">Frete e cupons no pagamento. Incluir um livro renova o prazo da reserva.</p>
            <Botao
              disabled={iniciandoPagamento || carrinho.itens.some((item) => !item.precoUnitario)}
              onClick={() => void irParaPagamento()}
            >
              Ir para pagamento
            </Botao>
            <BotaoLink variante="secundario" href="/">
              Continuar comprando
            </BotaoLink>
          </Painel>
        </div>
      )}

      {carrinho && carrinho.removidos.length > 0 && (
        <ItensRemovidos
          removidos={carrinho.removidos}
          readicionar={(removido) => void readicionar(removido)}
          dispensar={dispensarRemovido}
        />
      )}
    </section>
  );
}
