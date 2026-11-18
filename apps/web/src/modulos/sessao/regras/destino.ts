import type { Usuario } from '../api';

function naArea(pathname: string, area: string): boolean {
  return pathname === area || pathname.startsWith(`${area}/`);
}

/**
 * Destino de retorno depois do login. Só aceita um caminho da mesma origem e
 * coerente com o papel do usuário; qualquer outro valor cai na área padrão.
 */
export function destinoSeguro(
  destino: string | undefined,
  usuario: Usuario,
  origem: string,
): string {
  const inicial = usuario.papel === 'ADMIN' ? '/admin/clientes' : '/minha-conta';

  if (!destino || !destino.startsWith('/') || destino.startsWith('//') || destino.includes('\\')) {
    return inicial;
  }

  let alvo: URL;
  try {
    alvo = new URL(destino, origem);
  } catch {
    return inicial;
  }

  if (alvo.origin !== origem || !alvo.pathname.startsWith('/')) {
    return inicial;
  }
  if (naArea(alvo.pathname, '/admin') && usuario.papel !== 'ADMIN') {
    return inicial;
  }
  if (naArea(alvo.pathname, '/minha-conta') && usuario.papel !== 'CLIENTE') {
    return inicial;
  }
  return destino;
}
