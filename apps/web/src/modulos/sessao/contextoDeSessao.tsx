import { createContext } from 'preact';
import type { ComponentChildren } from 'preact';
import { useContext, useEffect, useRef, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { definirTratadorDeNaoAutorizado } from '../../api/cliente';
import { obterSessao, sair, type Usuario } from './api';

interface ContextoDeSessao {
  usuario: Usuario | null;
  carregando: boolean;
  definirUsuario: (usuario: Usuario | null) => void;
  encerrarSessao: () => Promise<void>;
  /** Limpa a sessão local e volta à vitrine, sem chamar o logout (o server já a encerrou). */
  voltarAVitrineSemSessao: () => Promise<void>;
}

const Contexto = createContext<ContextoDeSessao | null>(null);

/** Pré-carrega uma tela sob demanda (o `preload` de um `lazy` do preact-iso). */
type PreCarregar = () => Promise<unknown>;

interface Props {
  children: ComponentChildren;
  /** Telas carregadas antes de navegar, para o Router trocar de rota no mesmo commit. */
  preCarregar: { login: PreCarregar; vitrine: PreCarregar };
}

export function SessaoProvider({ children, preCarregar }: Props) {
  const [usuario, definirUsuario] = useState<Usuario | null>(null);
  const [carregando, definirCarregando] = useState(true);
  const { route } = useLocation();
  // Marcado de forma síncrona ao sair ou inativar, antes de qualquer await: um 401 que
  // chegue nesse meio-tempo (ex.: o stream SSE fechado pelo server) não leva ao login.
  const encerrando = useRef(false);

  // GET /api/sessao ao iniciar; qualquer falha é tratada como "sem sessão".
  useEffect(() => {
    let ativo = true;

    obterSessao()
      .then((encontrado) => {
        if (ativo) {
          definirUsuario(encontrado);
        }
      })
      .catch(() => {
        if (ativo) {
          definirUsuario(null);
        }
      })
      .finally(() => {
        if (ativo) {
          definirCarregando(false);
        }
      });

    return () => {
      ativo = false;
    };
  }, []);

  // Qualquer 401 da API limpa a sessão e volta ao login (DEC-08), guardando o
  // caminho atual para retornar depois de entrar.
  useEffect(() => {
    return definirTratadorDeNaoAutorizado(() => {
      if (encerrando.current) {
        return;
      }
      if (location.pathname === '/login') {
        definirUsuario(null);
        return;
      }

      const destino = `${location.pathname}${location.search}`;
      void preCarregar.login()
        .catch(() => undefined)
        .then(() => {
          if (encerrando.current) {
            return;
          }
          definirUsuario(null);
          route(`/login?destino=${encodeURIComponent(destino)}`, true);
        });
    });
  }, [route]);

  function definirUsuarioDaSessao(novo: Usuario | null): void {
    encerrando.current = false;
    definirUsuario(novo);
  }

  async function encerrarSessao(): Promise<void> {
    encerrando.current = true;
    try {
      await sair();
    } catch {
      // Mesmo se o server falhar, a sessão local é encerrada.
    }
    await voltarAVitrineSemSessao();
  }

  async function voltarAVitrineSemSessao(): Promise<void> {
    encerrando.current = true;
    // Garante que a vitrine já está carregada antes de limpar o usuário: assim o
    // Router troca de rota no mesmo commit e a Protegida não redireciona ao login.
    await preCarregar.vitrine().catch(() => undefined);

    definirUsuario(null);
    route('/', true);
  }

  return (
    <Contexto.Provider value={{ usuario, carregando, definirUsuario: definirUsuarioDaSessao, encerrarSessao, voltarAVitrineSemSessao }}>
      {children}
    </Contexto.Provider>
  );
}

export function useSessao(): ContextoDeSessao {
  const contexto = useContext(Contexto);
  if (!contexto) {
    throw new Error('useSessao precisa estar dentro de SessaoProvider.');
  }
  return contexto;
}
