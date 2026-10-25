import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { Erro } from '../../../ui/Erro';
import { OpcoesMarcaveis } from '../../../ui/OpcoesMarcaveis';
import { GENEROS, ORDENS, SITUACOES, UFS, consultaDoEstado, filtrosVazios, lerEstadoDaConsulta, rotuloDe, urlDaConsulta, type CampoDeTexto, type FiltrosDeClientes } from '../regras/filtrosDeClientes';
import { formatarCpf, formatarData } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';
import { Paginacao } from '../../../ui/Paginacao';

type PaginaDeClientes = components['schemas']['PaginaClienteListadoDto'];

interface Resultado {
  busca: string;
  pagina: PaginaDeClientes | null;
  problema: ProblemaDetalhe | null;
}

function alternado<T>(lista: T[], valor: T): T[] {
  return lista.includes(valor) ? lista.filter((item) => item !== valor) : [...lista, valor];
}

/** Consulta de clientes do administrador, com filtros combinados guardados na URL (RF-CLI-004). */
export default function AdminClientes() {
  const { url, route } = useLocation();
  const busca = url.includes('?') ? url.slice(url.indexOf('?')) : '';
  const estado = useMemo(() => lerEstadoDaConsulta(busca), [busca]);
  const [filtros, definirFiltros] = useState<FiltrosDeClientes>(estado.filtros);
  const [resultado, definirResultado] = useState<Resultado | null>(null);

  useEffect(() => {
    definirFiltros(estado.filtros);
  }, [estado]);

  useEffect(() => {
    const controle = new AbortController();
    cliente
      .GET('/api/admin/clientes', {
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
            problema: { status: 0, detail: 'Não foi possível carregar os clientes. Tente novamente.' },
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
  const ufSelecionada = filtros.uf[0] ?? '';

  return (
    <section class="pagina">
      <header>
        <h1>Clientes</h1>
        <p class="pagina__introducao">
          Consulte os clientes por qualquer combinação dos campos. O código é o número do cadastro.
        </p>
      </header>

      <form class="cartao filtros" role="search" aria-label="Filtrar clientes" onSubmit={filtrar}>
        <CampoDeFormulario id="filtro-nome" rotulo="Nome" valor={filtros.nome} name="nome"
          aoMudar={(valor) => mudarCampo('nome', valor)} />
        <CampoDeFormulario id="filtro-cpf" rotulo="CPF" valor={filtros.cpf} name="cpf" inputMode="numeric"
          aoMudar={(valor) => mudarCampo('cpf', valor)} />
        <CampoDeFormulario id="filtro-email" rotulo="E-mail" valor={filtros.email} name="email"
          aoMudar={(valor) => mudarCampo('email', valor)} />
        <CampoDeFormulario id="filtro-telefone" rotulo="Telefone" valor={filtros.telefone} name="telefone"
          inputMode="tel" aoMudar={(valor) => mudarCampo('telefone', valor)} />
        <CampoDeFormulario id="filtro-nascimento-de" rotulo="Nascimento de" tipo="date" name="dataNascimentoDe"
          valor={filtros.dataNascimentoDe} aoMudar={(valor) => mudarCampo('dataNascimentoDe', valor)} />
        <CampoDeFormulario id="filtro-nascimento-ate" rotulo="Nascimento até" tipo="date" name="dataNascimentoAte"
          valor={filtros.dataNascimentoAte} aoMudar={(valor) => mudarCampo('dataNascimentoAte', valor)} />
        <CampoDeFormulario id="filtro-cidade" rotulo="Cidade" valor={filtros.cidade} name="cidade"
          aoMudar={(valor) => mudarCampo('cidade', valor)} />
        <div class="campo">
          <label class="campo__rotulo" for="filtro-uf">UF</label>
          <select
            id="filtro-uf"
            class="campo__entrada"
            name="uf"
            value={ufSelecionada}
            onChange={(evento) => {
              const valor = evento.currentTarget.value;
              const uf = UFS.find((item) => item === valor);
              definirFiltros((atual) => ({ ...atual, uf: uf ? [uf] : [] }));
            }}
          >
            <option value="">Todas</option>
            {UFS.map((uf) => (
              <option key={uf} value={uf}>
                {uf}
              </option>
            ))}
          </select>
        </div>

        <OpcoesMarcaveis legenda="Gênero" nome="genero" opcoes={GENEROS} marcados={filtros.genero}
          alternar={(valor) => definirFiltros((atual) => ({ ...atual, genero: alternado(atual.genero, valor) }))} />
        <OpcoesMarcaveis legenda="Situação" nome="situacao" opcoes={SITUACOES} marcados={filtros.situacao}
          alternar={(valor) =>
            definirFiltros((atual) => ({ ...atual, situacao: alternado(atual.situacao, valor) }))} />

        <div class="acoes">
          <Botao type="submit">
            Filtrar
          </Botao>
          <Botao variante="secundario"
            type="button"
            onClick={() => {
              definirFiltros(filtrosVazios());
              route('/admin/clientes');
            }}
          >
            Limpar filtros
          </Botao>
        </div>
      </form>

      {visivel === null ? (
        <p class="aviso" role="status">
          Carregando clientes…
        </p>
      ) : visivel.problema ? (
        <Erro problema={visivel.problema} />
      ) : pagina === null || pagina.itens.length === 0 ? (
        <p class="aviso">Nenhum cliente encontrado.</p>
      ) : (
        <>
          <div class="resultado__cabecalho">
            <p>{pagina.totalItens === 1 ? '1 cliente encontrado' : `${pagina.totalItens} clientes encontrados`}</p>
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
                  <th scope="col">Código</th>
                  <th scope="col">Nome</th>
                  <th scope="col">CPF</th>
                  <th scope="col">E-mail</th>
                  <th scope="col">Telefone</th>
                  <th scope="col">Nascimento</th>
                  <th scope="col">Gênero</th>
                  <th scope="col">Situação</th>
                  <th scope="col" class="tabela__numero">Ranking</th>
                </tr>
              </thead>
              <tbody>
                {pagina.itens.map((item) => (
                  <tr key={item.id}>
                    <td>{item.id}</td>
                    <td>
                      <a href={`/admin/clientes/${item.id}`}>{item.nome}</a>
                    </td>
                    <td>{formatarCpf(item.cpf)}</td>
                    <td>{item.email}</td>
                    <td>{item.telefone ?? '—'}</td>
                    <td>{formatarData(item.dataNascimento)}</td>
                    <td>{rotuloDe(GENEROS, item.genero)}</td>
                    <td>{rotuloDe(SITUACOES, item.situacao)}</td>
                    <td class="tabela__numero">{item.ranking}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <Paginacao pagina={estado.pagina} totalPaginas={pagina.totalPaginas} rotulo="Páginas de clientes" urlDaPagina={(numero) => urlDaConsulta({ ...estado, pagina: numero })} />
        </>
      )}
    </section>
  );
}
