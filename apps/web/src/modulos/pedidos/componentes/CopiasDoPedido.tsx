import type { components } from '../../../api/esquema';
import { formatarCep } from '../../conta/regras/cadastroDeCliente';
import { formatarCpf, formatarDinheiro, textoDoStatus } from '../../../compartilhado/formatacao';

type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];
type EnderecoNoPedido = components['schemas']['EnderecoNoPedido'];

function Endereco({ titulo, endereco }: { titulo: string; endereco: EnderecoNoPedido }) {
  return (
    <div>
      <h3>{titulo}</h3>
      <p>
        {endereco.nomeCurto && <strong>{endereco.nomeCurto}: </strong>}
        {endereco.logradouro}, {endereco.numero} — {endereco.bairro}, {endereco.cidade}/{endereco.uf}, CEP{' '}
        {formatarCep(endereco.cep)}
        {endereco.observacoes ? ` (${endereco.observacoes})` : ''}
      </p>
    </div>
  );
}

/**
 * Cópias gravadas no pedido na finalização: cliente, endereços, itens, cartões, cupons e valores (DEC-12). Usadas
 * no detalhe do admin (RF-VEN-009) e em "Meus pedidos" (RF-CLI-005).
 */
export function CopiasDoPedido({ pedido, linkDoCliente }: { pedido: PedidoDetalhe; linkDoCliente?: string }) {
  return (
    <>
      <div class="cartao">
        <h2>Cliente</h2>
        <dl class="detalhe__campos">
          <div>
            <dt>Nome</dt>
            <dd>{linkDoCliente ? <a href={linkDoCliente}>{pedido.cliente.nome}</a> : pedido.cliente.nome}</dd>
          </div>
          <div><dt>CPF</dt><dd>{formatarCpf(pedido.cliente.cpf)}</dd></div>
          <div><dt>E-mail</dt><dd>{pedido.cliente.email}</dd></div>
          <div><dt>Telefone</dt><dd>{pedido.cliente.telefone ?? '—'}</dd></div>
        </dl>
      </div>

      <div class="cartao">
        <h2>Endereços</h2>
        <Endereco titulo="Entrega" endereco={pedido.enderecoEntrega} />
        <Endereco titulo="Cobrança" endereco={pedido.enderecoCobranca} />
      </div>

      <div class="cartao">
        <h2>Itens</h2>
        <div class="tabela-rolavel">
          <table class="tabela">
            <thead>
              <tr>
                <th scope="col">Livro</th>
                <th scope="col">Preço unitário</th>
                <th scope="col">Quantidade</th>
                <th scope="col">Subtotal</th>
                <th scope="col">Troca</th>
              </tr>
            </thead>
            <tbody>
              {pedido.itens.map((item) => (
                <tr key={item.id}>
                  <td>
                    <strong>{item.livro.titulo}</strong>
                    <br />
                    {item.livro.autores.join(', ')} — {item.livro.editora}, {item.livro.edicao} edição, ISBN{' '}
                    {item.livro.isbn}
                    <br />
                    {item.livro.categorias.map((categoria) => categoria.nome).join(', ')}
                  </td>
                  <td>{formatarDinheiro(item.precoUnitario)}</td>
                  <td>{item.quantidade}</td>
                  <td>{formatarDinheiro(item.subtotal)}</td>
                  <td>{item.statusDaTroca ? textoDoStatus(item.statusDaTroca) : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div class="cartao">
        <h2>Pagamento</h2>
        <ul class="lista">
          {pedido.cartoes.map((pagamento, indice) => (
            <li key={indice}>
              {pagamento.cartao.bandeira} final {pagamento.cartao.ultimosDigitos}:{' '}
              {formatarDinheiro(pagamento.valor)}
            </li>
          ))}
          {pedido.cupons.map((uso) => (
            <li key={`${uso.cupom.categoria}-${uso.cupom.id}`}>
              Cupom {uso.cupom.categoria === 'TROCA' ? 'de troca' : 'promocional'} {uso.cupom.codigo}:{' '}
              {formatarDinheiro(uso.valorAbatido)}
            </li>
          ))}
        </ul>
        <dl class="detalhe__campos">
          <div><dt>Subtotal dos itens</dt><dd>{formatarDinheiro(pedido.subtotal)}</dd></div>
          <div><dt>Frete</dt><dd>{formatarDinheiro(pedido.frete)}</dd></div>
          <div><dt>Desconto promocional</dt><dd>{formatarDinheiro(pedido.descontoPromocional)}</dd></div>
          <div><dt>Total</dt><dd data-testid="total-do-pedido">{formatarDinheiro(pedido.total)}</dd></div>
        </dl>
      </div>
    </>
  );
}
