import { useLocation } from 'preact-iso';

const secoes = [
  { caminho: '/admin', rotulo: 'Resumo' },
  { caminho: '/admin/clientes', rotulo: 'Clientes' },
  { caminho: '/admin/livros', rotulo: 'Livros' },
  { caminho: '/admin/pedidos', rotulo: 'Pedidos' },
  { caminho: '/admin/trocas', rotulo: 'Trocas' },
  { caminho: '/admin/analise', rotulo: 'Análise de vendas' },
];

export default function Admin() {
  const { path } = useLocation();

  return (
    <section class="pagina">
      <h1>Administração</h1>
      <p class="pagina__introducao">
        Área restrita ao administrador da loja. As telas de gestão entram nas próximas etapas.
      </p>

      <nav class="abas" aria-label="Seções da administração">
        {secoes.map((secao) => (
          <a
            key={secao.caminho}
            href={secao.caminho}
            class={path === secao.caminho ? 'aba aba--ativa' : 'aba'}
            aria-current={path === secao.caminho ? 'page' : undefined}
          >
            {secao.rotulo}
          </a>
        ))}
      </nav>

      <div class="cartao">
        <h2>Em construção</h2>
        <p>
          Clientes, pedidos e trocas já podem ser consultados pelas abas acima. Em breve você poderá ver a
          análise de vendas com gráficos e exportação.
        </p>
      </div>
    </section>
  );
}
