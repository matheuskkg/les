import { useEffect, useMemo, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { formatarData } from '../../../compartilhado/formatacao';
import { useCarregamento } from '../../../compartilhado/useCarregamento';
import { Botao } from '../../../ui/Botao';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { Erro } from '../../../ui/Erro';
import { Violacoes } from '../../../ui/Violacoes';
import { CadeiaDeProvedoresIa } from '../componentes/CadeiaDeProvedoresIa';
import { GraficoDeChamadas } from '../componentes/GraficoDeChamadas';
import {
  formatarChamadasHttp,
  formatarInteiro,
  formatarLatencia,
  formatarTaxa,
  lerPeriodoDasMetricas,
  rotuloDoDia,
  rotuloDoResultado,
  urlDoAssistente,
  type PeriodoDasMetricas,
} from '../regras/assistenteDoAdmin';

type ProvedorIa = components['schemas']['ProvedorIaDto'];

/**
 * Aba "Assistente" do administrador (RNF-REC-001, DEC-03): a cadeia de provedores de IA, alterável sem reiniciar o
 * server, e as métricas de funcionamento por período, com o gráfico diário de chamadas e falhas. O período fica na
 * URL; as regras dele são do server, que responde 422 com a violação.
 */
export default function AdminAssistente() {
  const { url, route } = useLocation();
  const busca = url.includes('?') ? url.slice(url.indexOf('?')) : '';
  const periodo = useMemo(() => lerPeriodoDasMetricas(busca, new Date()), [busca]);
  const [filtros, definirFiltros] = useState<PeriodoDasMetricas>(periodo);
  const [cadeia, definirCadeia] = useState<ProvedorIa[] | null>(null);

  useEffect(() => definirFiltros(periodo), [periodo]);

  const provedores = useCarregamento((signal) => cliente.GET('/api/admin/ia/provedores', { signal }), []);
  const metricas = useCarregamento(
    (signal) => cliente.GET('/api/admin/ia/metricas', { params: { query: periodo }, signal }),
    [periodo.inicio, periodo.fim],
  );

  useEffect(() => {
    if (provedores.dados) {
      definirCadeia(provedores.dados);
    }
  }, [provedores.dados]);

  const dados = metricas.dados;
  const dias = useMemo(() => (dados?.serie ?? []).map((dia) => rotuloDoDia(dia.dia)), [dados]);
  const chamadas = useMemo(() => (dados?.serie ?? []).map((dia) => dia.chamadas), [dados]);
  const falhas = useMemo(() => (dados?.serie ?? []).map((dia) => dia.falhas), [dados]);

  function filtrar(evento: Event) {
    evento.preventDefault();
    route(urlDoAssistente(filtros));
  }

  return (
    <section class="pagina">
      <header>
        <h1>Assistente</h1>
        <p class="pagina__introducao">
          Provedores de IA da vitrine e do chatbot e o funcionamento de cada um. A chave e o endereço de cada
          provedor ficam só na configuração do server.
        </p>
      </header>

      <Erro problema={provedores.problema} />
      {cadeia === null ? (
        provedores.carregando && (
          <p class="aviso" role="status">
            Carregando provedores…
          </p>
        )
      ) : (
        <CadeiaDeProvedoresIa gravada={cadeia} aoSalvar={definirCadeia} />
      )}

      <h2>Métricas</h2>
      <form class="cartao filtros" aria-label="Período das métricas" onSubmit={filtrar}>
        <CampoDeFormulario id="metricas-inicio" rotulo="Dia inicial" tipo="date" name="inicio" required
          valor={filtros.inicio} aoMudar={(valor) => definirFiltros((atual) => ({ ...atual, inicio: valor }))} />
        <CampoDeFormulario id="metricas-fim" rotulo="Dia final" tipo="date" name="fim" required
          valor={filtros.fim} aoMudar={(valor) => definirFiltros((atual) => ({ ...atual, fim: valor }))} />
        <div class="acoes">
          <Botao type="submit">Atualizar</Botao>
          <Botao
            variante="secundario"
            onClick={() => {
              metricas.recarregar();
              provedores.recarregar();
            }}
          >
            Recarregar
          </Botao>
        </div>
      </form>

      {metricas.problema ? (
        <>
          <Violacoes violacoes={metricas.problema.violacoes} />
          {!metricas.problema.violacoes?.length && <Erro problema={metricas.problema} />}
        </>
      ) : dados === null ? (
        <p class="aviso" role="status">
          Carregando métricas…
        </p>
      ) : (
        <>
          <div class="cartao">
            <h3>
              Chamadas de {formatarData(dados.inicio)} a {formatarData(dados.fim)}
            </h3>
            <GraficoDeChamadas dias={dias} chamadas={chamadas} falhas={falhas} />
            <details class="metricas__dias">
              <summary>Ver chamadas por dia</summary>
              <div class="tabela-rolavel">
                <table class="tabela" aria-label="Chamadas e falhas por dia">
                  <thead>
                    <tr>
                      <th scope="col">Dia</th>
                      <th scope="col" class="tabela__numero">Chamadas</th>
                      <th scope="col" class="tabela__numero">Falhas</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dados.serie.map((dia) => (
                      <tr key={dia.dia}>
                        <th scope="row">{formatarData(dia.dia)}</th>
                        <td class="tabela__numero">{formatarInteiro(dia.chamadas)}</td>
                        <td class="tabela__numero">{formatarInteiro(dia.falhas)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </details>
          </div>

          <p class="metricas__sem-ia">
            Recomendações da vitrine gravadas sem IA no período: <b>{formatarInteiro(dados.recomendacoesSemIa)}</b>
          </p>

          <div class="tabela-rolavel">
            <table class="tabela" aria-label="Métricas por provedor">
              <thead>
                <tr>
                  <th scope="col">Provedor</th>
                  <th scope="col" class="tabela__numero">Chamadas</th>
                  <th scope="col" class="tabela__numero">Chamadas HTTP/mensagem</th>
                  <th scope="col" class="tabela__numero">Sucesso</th>
                  <th scope="col">Falhas</th>
                  <th scope="col" class="tabela__numero">Latência média</th>
                  <th scope="col" class="tabela__numero">Latência p95</th>
                  <th scope="col" class="tabela__numero">Tokens (entrada / saída)</th>
                  <th scope="col" class="tabela__numero">Por reserva</th>
                  <th scope="col" class="tabela__numero">Aprovação</th>
                </tr>
              </thead>
              <tbody>
                {dados.provedores.map((provedor) => (
                  <tr key={provedor.id}>
                    <td>
                      <strong>{provedor.nome}</strong>
                    </td>
                    <td class="tabela__numero">{formatarInteiro(provedor.chamadas)}</td>
                    <td class="tabela__numero">
                      {formatarChamadasHttp(provedor.mediaDeChamadasHttp, provedor.maximoDeChamadasHttp)}
                    </td>
                    <td class="tabela__numero">{formatarTaxa(provedor.taxaDeSucesso)}</td>
                    <td>
                      {provedor.falhasPorResultado.length === 0 ? (
                        '—'
                      ) : (
                        <ul class="metricas__falhas">
                          {provedor.falhasPorResultado.map((falha) => (
                            <li key={falha.resultado}>
                              {rotuloDoResultado(falha.resultado)}: {formatarInteiro(falha.quantidade)}
                            </li>
                          ))}
                        </ul>
                      )}
                    </td>
                    <td class="tabela__numero">{formatarLatencia(provedor.latenciaMediaMs)}</td>
                    <td class="tabela__numero">{formatarLatencia(provedor.latenciaP95Ms)}</td>
                    <td class="tabela__numero">
                      {formatarInteiro(provedor.tokensEntrada)} / {formatarInteiro(provedor.tokensSaida)}
                    </td>
                    <td class="tabela__numero">{formatarInteiro(provedor.atendimentosPorReserva)}</td>
                    <td class="tabela__numero">
                      {formatarTaxa(provedor.taxaDeAprovacao)}
                      {provedor.avaliadas > 0 && (
                        <span class="metricas__detalhe">
                          {formatarInteiro(provedor.positivas)} de {formatarInteiro(provedor.avaliadas)}
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </section>
  );
}
