import type { ComponentChildren } from 'preact';
import { useLocation } from 'preact-iso';

const secoes = [
  { caminho: '/admin', rotulo: 'Resumo' },
  { caminho: '/admin/clientes', rotulo: 'Clientes' },
  { caminho: '/admin/livros', rotulo: 'Livros' },
  { caminho: '/admin/pedidos', rotulo: 'Pedidos' },
  { caminho: '/admin/trocas', rotulo: 'Trocas' },
  { caminho: '/admin/analise', rotulo: 'Análise de vendas' },
];

/** A seção ativa inclui as telas de detalhe dela (`/admin/pedidos/12` fica em Pedidos); o resumo só na própria rota. */
function ativa(caminho: string, path: string) {
  return path === caminho || (caminho !== '/admin' && path.startsWith(`${caminho}/`));
}

/** Abas das seções da administração, acima de todas as telas do administrador. */
export function NavegacaoDoAdmin({ children }: { children: ComponentChildren }) {
  const { path } = useLocation();

  return (
    <div class="area-do-admin">
      <nav class="abas" aria-label="Seções da administração">
        {secoes.map((secao) => (
          <a
            key={secao.caminho}
            href={secao.caminho}
            class={ativa(secao.caminho, path) ? 'aba aba--ativa' : 'aba'}
            aria-current={path === secao.caminho ? 'page' : undefined}
          >
            {secao.rotulo}
          </a>
        ))}
      </nav>
      {children}
    </div>
  );
}
