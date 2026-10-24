import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { CampoDeFormulario } from '../componentes/CampoDeFormulario';
import { Erro } from '../componentes/Erro';
import { formatarCpf } from '../filtrosDeClientes';
import {
  ORDENS,
  STATUS,
  consultaDoEstado,
  filtrosVazios,
  lerEstadoDaConsulta,
  numeroValido,
  urlDaConsulta,
  type CampoDeTexto,
  type FiltrosDePedidos,
} from '../filtrosDePedidos';
import { formatarDataHora, formatarDinheiro, textoDoStatus } from '../formatacao';

type PaginaDePedidos = components['schemas']['PaginaPedidoListadoDto'];

interface Resultado {
  busca: string;
  pagina: PaginaDePedidos | null;
  problema: ProblemaDetalhe | null;
}

const NUMERO_INVALIDO = 'O número do pedido deve ter só dígitos.';
const CODIGO_INVALIDO = 'O código do cliente deve ter só dígitos.';

/** Consulta de pedidos do administrador, com filtros combinados guardados na URL (RF-VEN-009, RF-VEN-010). */
export default function AdminPedidos() {
  const { url, route } = useLocation();
  const busca = url.includes('?') ? url.slice(url.indexOf('?')) : '';
  const estado = useMemo(() => lerEstadoDaConsulta(busca), [busca]);
  const [filtros, definirFiltros] = useState<FiltrosDePedidos>(estado.filtros);
  const [resultado, definirResultado] = useState<Resultado | null>(null);

  useEffect(() => {
    definirFiltros(estado.filtros);
  }, [estado]);

  useEffect(() => {
    if (!numeroValido(estado.filtros.numero) || !numeroValido(estado.filtros.clienteId)) {
      const detail = numeroValido(estado.filtros.numero) ? CODIGO_INVALIDO : NUMERO_INVALIDO;
      definirResultado({ busca, pagina: null, problema: { status: 400, detail } });
      return;
    }
    const controle = new AbortController();
    cliente
      .GET('/api/admin/pedidos', {
        params: { query: consultaDoEstado(estado) },
        signal: controle.signal,
      })
      .then(({ data, error, response }) => {
        if (controle.signal.aborted) {
          return;
        }
        definirResultado(
          response.ok
            ? { busca, pagina: data ?? null, problema: null }
            : { busca, pagina: null, problema: interpretarProblema(error, response.status) },
        );
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirResultado({
            busca,
            pagina: null,
            problema: { status: 0, detail: 'Não foi possível carregar os pedidos. Tente novamente.' },
          });
        }
      });
    return () => controle.abort();
  }, [busca, estado]);

  function mudarCampo(campo: CampoDeTexto, valor: string) {
    definirFiltros((atual) => ({ ...atual, [campo]: valor }));
  }

  function filtrar(evento: Event) {
    evento.preventDefault();
    route(urlDaConsulta({ filtros, ordem: estado.ordem, pagina: 1 }));
  }

  const visivel = resultado?.busca === busca ? resultado : null;
  const pagina = visivel?.pagina ?? null;

  return (
    <section class="pagina">
      <header>
        <h1>Pedidos</h1>
        <p class="pagina__introducao">
          Consulte os pedidos por qualquer combinação dos campos. O período considera a data da finalização.
        </p>
      </header>

      <form class="cartao vitrine__filtros" role="search" aria-label="Filtrar pedidos" onSubmit={filtrar}>
        <CampoDeFormulario id="filtro-numero" rotulo="Número do pedido" valor={filtros.numero} name="numero"
          inputMode="numeric" erro={numeroValido(filtros.numero) ? undefined : NUMERO_INVALIDO}
          aoMudar={(valor) => mudarCampo('numero', valor)} />
        <CampoDeFormulario id="filtro-cliente-id" rotulo="Código do cliente" valor={filtros.clienteId}
          name="clienteId" inputMode="numeric"
          erro={numeroValido(filtros.clienteId) ? undefined : CODIGO_INVALIDO}
          aoMudar={(valor) => mudarCampo('clienteId', valor)} />
        <CampoDeFormulario id="filtro-cliente" rotulo="Cliente" valor={filtros.cliente} name="cliente"
          aoMudar={(valor) => mudarCampo('cliente', valor)} />
        <CampoDeFormulario id="filtro-cpf" rotulo="CPF" valor={filtros.cpf} name="cpf" inputMode="numeric"
          aoMudar={(valor) => mudarCampo('cpf', valor)} />
        <CampoDeFormulario id="filtro-realizado-de" rotulo="Realizado de" tipo="date" name="realizadoEmDe"
          valor={filtros.realizadoEmDe} aoMudar={(valor) => mudarCampo('realizadoEmDe', valor)} />
        <CampoDeFormulario id="filtro-realizado-ate" rotulo="Realizado até" tipo="date" name="realizadoEmAte"
          valor={filtros.realizadoEmAte} aoMudar={(valor) => mudarCampo('realizadoEmAte', valor)} />

        <fieldset class="grupo-de-categorias">
          <legend>Status</legend>
          <div class="categorias">
            {STATUS.map((opcao) => (
              <label class="categoria-opcao" key={opcao.valor}>
                <input
                  type="checkbox"
                  name="status"
                  value={opcao.valor}
                  checked={filtros.status.includes(opcao.valor)}
                  onChange={() =>
                    definirFiltros((atual) => ({
                      ...atual,
                      status: atual.status.includes(opcao.valor)
                        ? atual.status.filter((item) => item !== opcao.valor)
                        : STATUS.map((item) => item.valor)
                          .filter((valor) => valor === opcao.valor || atual.status.includes(valor)),
                    }))}
                />
                {opcao.rotulo}
              </label>
            ))}
          </div>
        </fieldset>

        <div class="acoes">
          <button class="botao" type="submit" disabled={!numeroValido(filtros.numero) || !numeroValido(filtros.clienteId)}>
            Filtrar
          </button>
          <button
            class="botao botao--secundario"
            type="button"
            onClick={() => {
              definirFiltros(filtrosVazios());
              route('/admin/pedidos');
            }}
          >
            Limpar filtros
          </button>
        </div>
      </form>

      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando pedidos…
        </p>
      ) : visivel.problema ? (
        <Erro problema={visivel.problema} />
      ) : pagina !== null && pagina.itens.length === 0 && pagina.totalItens > 0 ? (
        <p class="aviso">
          Esta página não existe mais. <a href={urlDaConsulta({ ...estado, pagina: 1 })}>Ir para a primeira página</a>
        </p>
      ) : pagina === null || pagina.itens.length === 0 ? (
        <p class="aviso">Nenhum pedido encontrado.</p>
      ) : (
        <>
          <div class="resultado__cabecalho">
            <p>{pagina.totalItens === 1 ? '1 pedido encontrado' : `${pagina.totalItens} pedidos encontrados`}</p>
            <label>
              Ordenar por{' '}
              <select
                value={estado.ordem}
                onChange={(evento) =>
                  route(urlDaConsulta({ ...estado, ordem: evento.currentTarget.value, pagina: 1 }))}
              >
                {ORDENS.map((ordem) => (
                  <option key={ordem.valor} value={ordem.valor}>
                    {ordem.rotulo}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <div class="tabela-rolavel">
            <table class="tabela">
              <thead>
                <tr>
                  <th scope="col">Número</th>
                  <th scope="col">Realizado em</th>
                  <th scope="col">Cliente</th>
                  <th scope="col">CPF</th>
                  <th scope="col">Status</th>
                  <th scope="col">Total</th>
                </tr>
              </thead>
              <tbody>
                {pagina.itens.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <a href={`/admin/pedidos/${item.id}`}>{item.id}</a>
                    </td>
                    <td>{formatarDataHora(item.realizadoEm)}</td>
                    <td>{item.clienteNome}</td>
                    <td>{formatarCpf(item.clienteCpf)}</td>
                    <td>{textoDoStatus(item.status)}</td>
                    <td>{formatarDinheiro(item.total)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {pagina.totalPaginas > 1 && (
            <nav class="paginacao" aria-label="Páginas de pedidos">
              {estado.pagina > 1 ? (
                <a class="botao botao--secundario" href={urlDaConsulta({ ...estado, pagina: estado.pagina - 1 })}>
                  Anterior
                </a>
              ) : (
                <span class="botao botao--secundario botao--inativo" aria-disabled="true">Anterior</span>
              )}
              <span class="paginacao__posicao">
                Página {estado.pagina} de {pagina.totalPaginas}
              </span>
              {estado.pagina < pagina.totalPaginas ? (
                <a class="botao botao--secundario" href={urlDaConsulta({ ...estado, pagina: estado.pagina + 1 })}>
                  Próxima
                </a>
              ) : (
                <span class="botao botao--secundario botao--inativo" aria-disabled="true">Próxima</span>
              )}
            </nav>
          )}
        </>
      )}
    </section>
  );
}
