import { useEffect, useState } from 'preact/hooks';
import { useRoute } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { Erro } from '../componentes/Erro';
import { formatarCpf } from '../filtrosDeClientes';
import { formatarDataHora, formatarDinheiro, textoDoStatus } from '../formatacao';

type PedidoDetalhe = components['schemas']['PedidoDetalheDto'];
type EnderecoNoPedido = components['schemas']['EnderecoNoPedido'];

interface Resultado {
  id: string;
  pedido: PedidoDetalhe | null;
  problema: ProblemaDetalhe | null;
}

function formatarCep(cep: string): string {
  const digitos = cep.replace(/\D/g, '');
  return digitos.length === 8 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : cep;
}

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
 * Detalhe do pedido para o administrador com as cópias gravadas na finalização: cliente, endereços, itens,
 * cartões, cupons e valores (RF-VEN-009, RF-VEN-010, DEC-12).
 */
export default function AdminPedido() {
  const { params } = useRoute();
  const id = params.id ?? '';
  const [resultado, definirResultado] = useState<Resultado | null>(null);

  useEffect(() => {
    const numero = Number(id);
    if (!/^\d+$/.test(id) || !Number.isSafeInteger(numero)) {
      definirResultado({ id, pedido: null, problema: { status: 404, detail: 'Pedido não encontrado.' } });
      return;
    }
    const controle = new AbortController();
    cliente
      .GET('/api/admin/pedidos/{id}', { params: { path: { id: numero } }, signal: controle.signal })
      .then(({ data, error, response }) => {
        if (!controle.signal.aborted) {
          definirResultado(
            response.ok
              ? { id, pedido: data ?? null, problema: null }
              : { id, pedido: null, problema: interpretarProblema(error, response.status) },
          );
        }
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirResultado({
            id,
            pedido: null,
            problema: { status: 0, detail: 'Não foi possível carregar o pedido. Tente novamente.' },
          });
        }
      });
    return () => controle.abort();
  }, [id]);

  const visivel = resultado?.id === id ? resultado : null;
  const pedido = visivel?.pedido ?? null;

  return (
    <section class="pagina">
      <p>
        <a href="/admin/pedidos">← Voltar para os pedidos</a>
      </p>

      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : pedido === null ? (
        <>
          <h1>Pedido não encontrado</h1>
          <Erro problema={visivel.problema} />
        </>
      ) : (
        <>
          <h1>Pedido {pedido.id}</h1>
          <div class="cartao">
            <h2>Situação</h2>
            <dl class="detalhe__campos">
              <div><dt>Status</dt><dd data-testid="status-do-pedido">{textoDoStatus(pedido.status)}</dd></div>
              <div><dt>Realizado em</dt><dd>{formatarDataHora(pedido.realizadoEm)}</dd></div>
              {pedido.entregueEm && (
                <div><dt>Entregue em</dt><dd>{formatarDataHora(pedido.entregueEm)}</dd></div>
              )}
            </dl>
          </div>

          <div class="cartao">
            <h2>Cliente</h2>
            <dl class="detalhe__campos">
              <div>
                <dt>Nome</dt>
                <dd><a href={`/admin/clientes/${pedido.cliente.id}`}>{pedido.cliente.nome}</a></dd>
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
      )}
    </section>
  );
}
