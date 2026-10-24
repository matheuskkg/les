import { useLocation } from 'preact-iso';

const secoes = [
  { caminho: '/minha-conta', rotulo: 'Resumo' },
  { caminho: '/minha-conta/dados', rotulo: 'Dados' },
  { caminho: '/minha-conta/enderecos', rotulo: 'Endereços' },
  { caminho: '/minha-conta/cartoes', rotulo: 'Cartões' },
  { caminho: '/minha-conta/pedidos', rotulo: 'Pedidos' },
];

export default function MinhaConta() {
  const { path } = useLocation();

  return (
    <section class="pagina">
      <h1>Minha conta</h1>
      <p class="pagina__introducao">
        Esta é a área do cliente. As telas de dados, endereços, cartões e pedidos entram nas
        próximas etapas.
      </p>

      <nav class="abas" aria-label="Seções da conta">
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
          Em breve você poderá consultar e alterar seus dados, gerenciar endereços e cartões e
          acompanhar seus pedidos por aqui.
        </p>
      </div>
    </section>
  );
}
