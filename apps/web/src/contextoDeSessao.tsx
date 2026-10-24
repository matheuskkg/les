import { createContext } from 'preact';
import type { ComponentChildren } from 'preact';
import { useContext, useEffect, useRef, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { definirTratadorDeNaoAutorizado } from './api/cliente';
import { Login, Vitrine } from './paginas/telas';
import { obterSessao, sair, type Usuario } from './sessao';

interface ContextoDeSessao {
  usuario: Usuario | null;
  carregando: boolean;
  definirUsuario: (usuario: Usuario | null) => void;
  encerrarSessao: () => Promise<void>;
  /** Limpa a sessão local e volta à vitrine, sem chamar o logout (o server já a encerrou). */
  voltarAVitrineSemSessao: () => Promise<void>;
}

const Contexto = createContext<ContextoDeSessao | null>(null);

export function SessaoProvider({ children }: { children: ComponentChildren }) {
  const [usuario, definirUsuario] = useState<Usuario | null>(null);
  const [carregando, definirCarregando] = useState(true);
  const { route } = useLocation();
  // O tratador de 401 é registrado uma vez; lê o usuário atual por aqui.
  const usuarioAtual = useRef<Usuario | null>(null);
  usuarioAtual.current = usuario;

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
      // Sem ninguém logado não há sessão a perder (ex.: o stream SSE fechado logo
      // depois de sair ou inativar a conta), então não há o que redirecionar.
      if (usuarioAtual.current === null) {
        return;
      }
      if (location.pathname === '/login') {
        definirUsuario(null);
        return;
      }

      const destino = `${location.pathname}${location.search}`;
      void Login.preload()
        .catch(() => undefined)
        .then(() => {
          if (usuarioAtual.current === null) {
            return;
          }
          definirUsuario(null);
          route(`/login?destino=${encodeURIComponent(destino)}`, true);
        });
    });
  }, [route]);

  async function encerrarSessao(): Promise<void> {
    try {
      await sair();
    } catch {
      // Mesmo se o server falhar, a sessão local é encerrada.
    }
    await voltarAVitrineSemSessao();
  }

  async function voltarAVitrineSemSessao(): Promise<void> {
    // Garante que a vitrine já está carregada antes de limpar o usuário: assim o
    // Router troca de rota no mesmo commit e a Protegida não redireciona ao login.
    await Vitrine.preload().catch(() => undefined);

    definirUsuario(null);
    route('/');
  }

  return (
    <Contexto.Provider value={{ usuario, carregando, definirUsuario, encerrarSessao, voltarAVitrineSemSessao }}>
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
