import { useEffect, useState } from 'preact/hooks';
import { useRoute } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { Erro } from '../../../ui/Erro';
import { PedidosDoCliente } from '../componentes/PedidosDoCliente';
import { GENEROS, SITUACOES, rotuloDe } from '../regras/filtrosDeClientes';
import { formatarCpf, formatarData } from '../../../compartilhado/formatacao';

type ClienteDetalhe = components['schemas']['ClienteDetalheDto'];

interface Resultado {
  id: string;
  cliente: ClienteDetalhe | null;
  problema: ProblemaDetalhe | null;
}

function formatarCep(cep: string): string {
  const digitos = cep.replace(/\D/g, '');
  return digitos.length === 8 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : cep;
}

function formatarValidade(validade: string): string {
  const partes = /^(\d{4})-(\d{2})$/.exec(validade);
  return partes ? `${partes[2]}/${partes[1]}` : validade;
}

/**
 * Detalhe do cliente para o administrador: dados, endereços, cartões mascarados (RF-CLI-004) e o histórico de
 * pedidos (RF-CLI-005).
 */
export default function AdminCliente() {
  const { params } = useRoute();
  const id = params.id ?? '';
  const [resultado, definirResultado] = useState<Resultado | null>(null);

  useEffect(() => {
    const codigo = Number(id);
    if (!Number.isSafeInteger(codigo) || codigo <= 0) {
      definirResultado({ id, cliente: null, problema: { status: 404, detail: 'Cliente não encontrado.' } });
      return;
    }
    const controle = new AbortController();
    cliente
      .GET('/api/admin/clientes/{id}', { params: { path: { id: codigo } }, signal: controle.signal })
      .then(({ data, error, response }) => {
        if (!controle.signal.aborted) {
          definirResultado(
            response.ok
              ? { id, cliente: data ?? null, problema: null }
              : { id, cliente: null, problema: interpretarProblema(error, response.status) },
          );
        }
      })
      .catch(() => undefined);
    return () => controle.abort();
  }, [id]);

  const visivel = resultado?.id === id ? resultado : null;

  return (
    <section class="pagina">
      <p>
        <a href="/admin/clientes">← Voltar para os clientes</a>
      </p>

      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : visivel.cliente === null ? (
        <>
          <h1>Cliente não encontrado</h1>
          <Erro problema={visivel.problema} />
        </>
      ) : (
        <>
          <h1>{visivel.cliente.nome}</h1>
          <div class="cartao">
            <h2>Dados</h2>
            <dl class="detalhe__campos">
              <div><dt>Código</dt><dd>{visivel.cliente.id}</dd></div>
              <div><dt>CPF</dt><dd>{formatarCpf(visivel.cliente.cpf)}</dd></div>
              <div><dt>E-mail</dt><dd>{visivel.cliente.email}</dd></div>
              <div><dt>Telefone</dt><dd>{visivel.cliente.telefone ?? '—'}</dd></div>
              <div><dt>Nascimento</dt><dd>{formatarData(visivel.cliente.dataNascimento)}</dd></div>
              <div><dt>Gênero</dt><dd>{rotuloDe(GENEROS, visivel.cliente.genero)}</dd></div>
              <div><dt>Situação</dt><dd>{rotuloDe(SITUACOES, visivel.cliente.situacao)}</dd></div>
              <div><dt>Ranking</dt><dd>{visivel.cliente.ranking} de 100</dd></div>
            </dl>
          </div>

          <div class="cartao">
            <h2>Endereços</h2>
            <ul class="lista">
              {visivel.cliente.enderecos.map((endereco) => (
                <li key={endereco.id}>
                  <strong>{endereco.nomeCurto}</strong> ({endereco.tipoEndereco === 'CASA' ? 'Casa' : 'Trabalho'}):{' '}
                  {endereco.logradouro}, {endereco.numero} — {endereco.bairro}, {endereco.cidade}/{endereco.uf},
                  CEP {formatarCep(endereco.cep)}
                  {endereco.observacoes ? ` (${endereco.observacoes})` : ''}
                </li>
              ))}
            </ul>
          </div>

          <div class="cartao">
            <h2>Cartões</h2>
            {visivel.cliente.cartoes.length === 0 ? (
              <p>Nenhum cartão cadastrado.</p>
            ) : (
              <ul class="lista">
                {visivel.cliente.cartoes.map((cartao) => (
                  <li key={cartao.id}>
                    {cartao.bandeira.nome} {cartao.numero} — {cartao.nomeImpresso}, validade{' '}
                    {formatarValidade(cartao.validade)}
                    {cartao.preferencial ? ' (preferencial)' : ''}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <PedidosDoCliente clienteId={visivel.cliente.id} />
        </>
      )}
    </section>
  );
}
