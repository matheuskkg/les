import { ErroDaApi, lerProblema, requisitar } from './api/cliente';

export type Papel = 'CLIENTE' | 'ADMIN';

/** Usuário logado, como devolvido por /api/sessao (DEC-08). */
export interface Usuario {
  usuarioId: number;
  email: string;
  papel: Papel;
}

function comoUsuario(dados: unknown): Usuario {
  const registro =
    typeof dados === 'object' && dados !== null ? (dados as Record<string, unknown>) : {};
  const papel = registro.papel;

  if (
    typeof registro.usuarioId !== 'number' ||
    typeof registro.email !== 'string' ||
    (papel !== 'CLIENTE' && papel !== 'ADMIN')
  ) {
    throw new ErroDaApi({
      status: 500,
      detail: 'Resposta de sessão inválida.',
    });
  }

  return {
    usuarioId: registro.usuarioId,
    email: registro.email,
    papel,
  };
}

/** POST /api/sessao — login por e-mail ou telefone (DEC-08/DEC-10). */
export async function entrar(login: string, senha: string): Promise<Usuario> {
  const resposta = await requisitar('/api/sessao', {
    method: 'POST',
    corpo: { login, senha },
  });

  if (!resposta.ok) {
    throw new ErroDaApi(await lerProblema(resposta));
  }

  return comoUsuario(await resposta.json());
}

/** GET /api/sessao — usuário logado, ou null quando não há sessão (401). */
export async function obterSessao(): Promise<Usuario | null> {
  const resposta = await requisitar('/api/sessao');

  if (resposta.status === 401) {
    return null;
  }
  if (!resposta.ok) {
    throw new ErroDaApi(await lerProblema(resposta));
  }

  return comoUsuario(await resposta.json());
}

/** DELETE /api/sessao — encerra a sessão no server. */
export async function sair(): Promise<void> {
  const resposta = await requisitar('/api/sessao', { method: 'DELETE' });

  if (!resposta.ok && resposta.status !== 401) {
    throw new ErroDaApi(await lerProblema(resposta));
  }
}
