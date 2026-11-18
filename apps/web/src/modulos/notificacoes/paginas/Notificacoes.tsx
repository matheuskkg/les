import { useCallback, useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import { Erro } from '../../../ui/Erro';
import { useNotificacoes } from '../contextoDeNotificacoes';
import { useSessao } from '../../sessao';
import { formatarDataHora } from '../../../compartilhado/formatacao';
import { linkDaReferencia, type Notificacao } from '../regras/notificacoes';
import type { Papel } from '../../sessao';
import { Botao } from '../../../ui/Botao';

const TAMANHO_DA_PAGINA = 20;

interface Pagina {
  itens: Notificacao[];
  pagina: number;
  totalPaginas: number;
  totalItens: number;
}

async function buscar(papel: Papel, pagina: number) {
  const query = { pagina, tamanho: TAMANHO_DA_PAGINA };
  return papel === 'ADMIN'
    ? cliente.GET('/api/admin/notificacoes', { params: { query } })
    : cliente.GET('/api/me/notificacoes', { params: { query } });
}

async function marcarComoLida(papel: Papel, id: number) {
  const params = { path: { id } };
  return papel === 'ADMIN'
    ? cliente.POST('/api/admin/notificacoes/{id}/ler', { params })
    : cliente.POST('/api/me/notificacoes/{id}/ler', { params });
}

async function marcarTodas(papel: Papel) {
  return papel === 'ADMIN'
    ? cliente.POST('/api/admin/notificacoes/ler-todas')
    : cliente.POST('/api/me/notificacoes/ler-todas');
}

/** Histórico de notificações do cliente ou do administrador: não lidas primeiro (DEC-13). */
export default function Notificacoes() {
  const { usuario } = useSessao();
  const { ouvir, recarregarContagem } = useNotificacoes();
  const [pagina, definirPagina] = useState(0);
  const [dados, definirDados] = useState<Pagina | null>(null);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const papel = usuario?.papel ?? null;

  const carregar = useCallback(async () => {
    if (papel === null) {
      return;
    }
    const { data, error, response } = await buscar(papel, pagina);
    if (!response.ok || !data) {
      definirProblema(interpretarProblema(error, response.status));
      return;
    }
    definirProblema(null);
    definirDados(data);
  }, [papel, pagina]);

  useEffect(() => {
    void carregar().catch(() => undefined);
  }, [carregar]);

  // Uma notificação nova chega pelo stream: a lista é recarregada.
  useEffect(() => ouvir(() => void carregar().catch(() => undefined)), [ouvir, carregar]);

  if (papel === null) {
    return null;
  }

  async function ler(id: number) {
    if (papel === null) {
      return;
    }
    const { error, response } = await marcarComoLida(papel, id);
    if (!response.ok) {
      definirProblema(interpretarProblema(error, response.status));
      return;
    }
    await Promise.all([carregar(), recarregarContagem()]);
  }

  async function lerTodas() {
    if (papel === null) {
      return;
    }
    const { error, response } = await marcarTodas(papel);
    if (!response.ok) {
      definirProblema(interpretarProblema(error, response.status));
      return;
    }
    await Promise.all([carregar(), recarregarContagem()]);
  }

  const temNaoLidas = dados?.itens.some((notificacao) => !notificacao.lidaEm) ?? false;

  return (
    <section class="pagina">
      <h1>Notificações</h1>
      <Erro problema={problema} />

      {dados === null ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : dados.itens.length === 0 ? (
        <p class="aviso">Nenhuma notificação por enquanto.</p>
      ) : (
        <>
          <div class="notificacoes__acoes">
            <Botao variante="secundario"
              type="button"
              disabled={!temNaoLidas}
              onClick={() => void lerTodas()}
            >
              Marcar todas como lidas
            </Botao>
          </div>

          <ul class="notificacoes">
            {dados.itens.map((notificacao) => {
              const link = linkDaReferencia(papel, notificacao.recurso, notificacao.recursoId);
              return (
                <li
                  key={notificacao.id}
                  class={notificacao.lidaEm ? 'notificacao' : 'notificacao notificacao--nao-lida'}
                >
                  <p class="notificacao__mensagem">
                    {link ? <a href={link}>{notificacao.mensagem}</a> : notificacao.mensagem}
                  </p>
                  <p class="notificacao__data">{formatarDataHora(notificacao.criadaEm)}</p>
                  {!notificacao.lidaEm && (
                    <Botao variante="secundario"
                      type="button"
                      onClick={() => void ler(notificacao.id)}
                    >
                      Marcar como lida
                    </Botao>
                  )}
                </li>
              );
            })}
          </ul>

          {dados.totalPaginas > 1 && (
            <nav class="paginacao" aria-label="Páginas de notificações">
              <div class="paginacao__central">
                <Botao variante="secundario"
                  type="button"
                  disabled={pagina === 0}
                  onClick={() => definirPagina(pagina - 1)}
                >
                  Anterior
                </Botao>
                <span class="paginacao__posicao">
                  Página {pagina + 1} de {dados.totalPaginas}
                </span>
                <Botao variante="secundario"
                  type="button"
                  disabled={pagina + 1 >= dados.totalPaginas}
                  onClick={() => definirPagina(pagina + 1)}
                >
                  Próxima
                </Botao>
              </div>
            </nav>
          )}
        </>
      )}
    </section>
  );
}
