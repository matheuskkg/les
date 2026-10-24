import { createContext } from 'preact';
import type { ComponentChildren } from 'preact';
import { useContext, useEffect, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { definirTratadorDeNaoAutorizado } from './api/cliente';
import { obterSessao, sair, type Usuario } from './sessao';

interface ContextoDeSessao {
  usuario: Usuario | null;
  carregando: boolean;
  definirUsuario: (usuario: Usuario | null) => void;
  encerrarSessao: () => Promise<void>;
}

const Contexto = createContext<ContextoDeSessao | null>(null);

export function SessaoProvider({ children }: { children: ComponentChildren }) {
  const [usuario, definirUsuario] = useState<Usuario | null>(null);
  const [carregando, definirCarregando] = useState(true);
  const { route } = useLocation();

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

  // Qualquer 401 da API limpa a sessão e volta ao login (DEC-08).
  useEffect(
    () =>
      definirTratadorDeNaoAutorizado(() => {
        definirUsuario(null);
        route('/login', true);
      }),
    [route],
  );

  async function encerrarSessao(): Promise<void> {
    try {
      await sair();
    } catch {
      // Mesmo se o server falhar, a sessão local é encerrada.
    }
    definirUsuario(null);
    route('/');
  }

  return (
    <Contexto.Provider value={{ usuario, carregando, definirUsuario, encerrarSessao }}>
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
