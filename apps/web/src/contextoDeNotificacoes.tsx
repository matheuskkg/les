import { createContext } from 'preact';
import type { ComponentChildren } from 'preact';
import { useCallback, useContext, useEffect, useRef, useState } from 'preact/hooks';
import { avisarSessaoEncerrada, cliente } from './api/cliente';
import { useSessao } from './contextoDeSessao';
import {
  EVENTO_DO_STREAM,
  lerNotificacaoDoEvento,
  prefixoDaArea,
  telaAfetada,
  type Notificacao,
  type TelaAfetada,
} from './notificacoes';
import { obterSessao } from './sessao';

type Ouvinte = (notificacao: Notificacao) => void;

interface ContextoDeNotificacoes {
  naoLidas: number;
  recarregarContagem: () => Promise<void>;
  ouvir: (ouvinte: Ouvinte) => () => void;
}

const Contexto = createContext<ContextoDeNotificacoes | null>(null);

/**
 * Contador de não lidas e stream SSE das notificações do usuário logado (DEC-13). O
 * `EventSource` reconecta sozinho e reenvia o `Last-Event-ID`; se a conexão for
 * recusada, confere a sessão e, sem ela, encerra a sessão local como num 401.
 */
export function NotificacoesProvider({ children }: { children: ComponentChildren }) {
  const { usuario } = useSessao();
  const [naoLidas, definirNaoLidas] = useState(0);
  const ouvintes = useRef(new Set<Ouvinte>());
  const papel = usuario?.papel ?? null;

  const recarregarContagem = useCallback(async () => {
    if (papel === null) {
      definirNaoLidas(0);
      return;
    }
    const caminho =
      papel === 'ADMIN' ? '/api/admin/notificacoes/nao-lidas' : '/api/me/notificacoes/nao-lidas';
    const { data } = await cliente.GET(caminho);
    if (data) {
      definirNaoLidas(data.quantidade);
    }
  }, [papel]);

  useEffect(() => {
    void recarregarContagem().catch(() => undefined);
  }, [recarregarContagem]);

  useEffect(() => {
    if (papel === null) {
      return;
    }

    const fonte = new EventSource(`${prefixoDaArea(papel)}/eventos`);

    fonte.addEventListener(EVENTO_DO_STREAM, (evento) => {
      const notificacao = lerNotificacaoDoEvento((evento as MessageEvent<string>).data);
      if (notificacao === null) {
        return;
      }
      if (!notificacao.lidaEm) {
        definirNaoLidas((atual) => atual + 1);
      }
      for (const ouvinte of ouvintes.current) {
        ouvinte(notificacao);
      }
    });

    // Um 401 fecha o EventSource sem reconexão; aí a sessão é conferida.
    fonte.onerror = () => {
      if (fonte.readyState !== EventSource.CLOSED) {
        return;
      }
      void obterSessao()
        .then((sessao) => {
          if (sessao === null) {
            avisarSessaoEncerrada();
          }
        })
        .catch(() => undefined);
    };

    return () => {
      fonte.close();
    };
  }, [papel]);

  const ouvir = useCallback((ouvinte: Ouvinte) => {
    ouvintes.current.add(ouvinte);
    return () => {
      ouvintes.current.delete(ouvinte);
    };
  }, []);

  return (
    <Contexto.Provider value={{ naoLidas, recarregarContagem, ouvir }}>{children}</Contexto.Provider>
  );
}

export function useNotificacoes(): ContextoDeNotificacoes {
  const contexto = useContext(Contexto);
  if (!contexto) {
    throw new Error('useNotificacoes precisa estar dentro de NotificacoesProvider.');
  }
  return contexto;
}

/**
 * Chama `aoChegar` quando chega uma notificação que afeta a tela informada, para que
 * ela recarregue os próprios dados (DEC-13).
 */
export function useRecargaPorNotificacao(tela: TelaAfetada, aoChegar: () => void): void {
  const { ouvir } = useNotificacoes();
  const atual = useRef(aoChegar);
  atual.current = aoChegar;

  useEffect(
    () =>
      ouvir((notificacao) => {
        if (telaAfetada(notificacao.tipo) === tela) {
          atual.current();
        }
      }),
    [ouvir, tela],
  );
}
