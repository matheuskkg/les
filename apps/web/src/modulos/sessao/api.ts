import { cliente, ErroDaApi, interpretarProblema } from '../../api/cliente';
import type { components } from '../../api/esquema';

export type Papel = components['schemas']['Sessao']['papel'];

/** Usuário logado, como devolvido por /api/sessao (DEC-08). */
export interface Usuario {
  usuarioId: number;
  email: string;
  papel: Papel;
  /** Nome de exibição: o do cliente, "Administração" ou, sem eles, o e-mail. */
  nome: string;
}

type SessaoDoContrato = components['schemas']['Sessao'];

function comoUsuario(dados: SessaoDoContrato | undefined): Usuario {
  if (
    dados === undefined ||
    typeof dados.usuarioId !== 'number' ||
    typeof dados.email !== 'string' ||
    (dados.papel !== 'CLIENTE' && dados.papel !== 'ADMIN') ||
    typeof dados.nome !== 'string'
  ) {
    throw new ErroDaApi({
      status: 500,
      detail: 'Resposta de sessão inválida.',
    });
  }

  return {
    usuarioId: dados.usuarioId,
    email: dados.email,
    papel: dados.papel,
    nome: dados.nome,
  };
}

/** POST /api/sessao — login por e-mail ou telefone (DEC-08/DEC-10). */
export async function entrar(login: string, senha: string): Promise<Usuario> {
  const { data, error, response } = await cliente.POST('/api/sessao', {
    body: { login, senha },
  });

  if (error !== undefined) {
    throw new ErroDaApi(interpretarProblema(error, response.status));
  }

  return comoUsuario(data);
}

/** GET /api/sessao — usuário logado, ou null quando não há sessão (401). */
export async function obterSessao(): Promise<Usuario | null> {
  const { data, error, response } = await cliente.GET('/api/sessao');

  if (response.status === 401) {
    return null;
  }
  if (error !== undefined) {
    throw new ErroDaApi(interpretarProblema(error, response.status));
  }

  return comoUsuario(data);
}

/** DELETE /api/sessao — encerra a sessão no server. */
export async function sair(): Promise<void> {
  const { error, response } = await cliente.DELETE('/api/sessao');

  if (!response.ok && response.status !== 401) {
    throw new ErroDaApi(interpretarProblema(error, response.status));
  }
}
