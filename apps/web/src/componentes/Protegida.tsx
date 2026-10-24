import type { ComponentChildren } from 'preact';
import { useEffect } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { useSessao } from '../contextoDeSessao';
import type { Papel } from '../sessao';

interface Props {
  papel: Papel;
  children: ComponentChildren;
}

/** Só renderiza a área quando a sessão existe e tem o papel exigido; senão, volta ao login. */
export function Protegida({ papel, children }: Props) {
  const { usuario, carregando } = useSessao();
  const { route, path } = useLocation();
  const autorizado = usuario?.papel === papel;

  useEffect(() => {
    if (carregando || autorizado) {
      return;
    }
    route(`/login?destino=${encodeURIComponent(path)}`, true);
  }, [carregando, autorizado, path, route]);

  if (carregando) {
    return (
      <p class="aviso" role="status">
        Carregando…
      </p>
    );
  }

  if (!autorizado) {
    return null;
  }

  return <>{children}</>;
}
