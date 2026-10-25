import type { ComponentChildren } from 'preact';
import { useEffect } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { useSessao } from '../contextoDeSessao';
import type { Papel } from '../api';

interface Props {
  papel: Papel;
  children: ComponentChildren;
}

/**
 * Só renderiza a área quando a sessão existe e tem o papel exigido. Sem sessão,
 * volta ao login; com sessão de outro papel, mostra "Acesso negado".
 */
export function Protegida({ papel, children }: Props) {
  const { usuario, carregando } = useSessao();
  const { route, path } = useLocation();
  const autorizado = usuario?.papel === papel;

  useEffect(() => {
    if (carregando || usuario !== null || autorizado) {
      return;
    }
    route(`/login?destino=${encodeURIComponent(path)}`, true);
  }, [carregando, usuario, autorizado, path, route]);

  if (carregando) {
    return (
      <p class="aviso" role="status">
        Carregando…
      </p>
    );
  }

  if (usuario === null) {
    return null;
  }

  if (!autorizado) {
    return (
      <section class="pagina pagina--estreita">
        <h1>Acesso negado</h1>
        <p class="pagina__introducao">
          Esta área não está disponível para o perfil da sua conta.
        </p>
        <p class="pagina__rodape">
          <a href="/">Voltar para a vitrine</a>.
        </p>
      </section>
    );
  }

  return <>{children}</>;
}
