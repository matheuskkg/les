import { useLocation } from 'preact-iso';
import { useSessao } from '../contextoDeSessao';
import { Cartoes } from './conta/Cartoes';
import { Dados } from './conta/Dados';
import { Enderecos } from './conta/Enderecos';
import { MeuPedido, MeusPedidos } from './conta/Pedidos';
import { Senha } from './conta/Senha';

const secoes = [
  { caminho: '/minha-conta', rotulo: 'Resumo' },
  { caminho: '/minha-conta/pedidos', rotulo: 'Meus pedidos' },
  { caminho: '/minha-conta/dados', rotulo: 'Meus dados' },
  { caminho: '/minha-conta/enderecos', rotulo: 'Endereços' },
  { caminho: '/minha-conta/cartoes', rotulo: 'Cartões' },
  { caminho: '/minha-conta/senha', rotulo: 'Senha' },
  { caminho: '/minha-conta/notificacoes', rotulo: 'Notificações' },
];

function Resumo() {
  const { usuario } = useSessao();
  return (
    <div class="cartao">
      <h2>Olá!</h2>
      <p>Você entrou como {usuario?.email}.</p>
      <ul class="lista">
        {secoes.slice(1).map((secao) => (
          <li key={secao.caminho}>
            <a href={secao.caminho}>{secao.rotulo}</a>
          </li>
        ))}
      </ul>
    </div>
  );
}

const DETALHE_DO_PEDIDO = /^\/minha-conta\/pedidos\/([^/]+)$/;

function Secao({ caminho }: { caminho: string }) {
  const detalhe = DETALHE_DO_PEDIDO.exec(caminho);
  if (detalhe?.[1] !== undefined) {
    return <MeuPedido id={detalhe[1]} />;
  }
  switch (caminho) {
    case '/minha-conta/pedidos':
      return <MeusPedidos />;
    case '/minha-conta/dados':
      return <Dados />;
    case '/minha-conta/enderecos':
      return <Enderecos />;
    case '/minha-conta/cartoes':
      return <Cartoes />;
    case '/minha-conta/senha':
      return <Senha />;
    default:
      return <Resumo />;
  }
}

/** A aba de "Meus pedidos" continua ativa no detalhe de um pedido. */
function ativa(path: string, caminho: string): boolean {
  return path === caminho || (caminho === '/minha-conta/pedidos' && DETALHE_DO_PEDIDO.test(path));
}

/**
 * Área do cliente: resumo, pedidos, dados cadastrais, endereços, cartões e senha (RF-CLI-002, RF-CLI-003,
 * RF-CLI-005, RF-CLI-006, RF-CLI-007, RF-CLI-008).
 */
export default function MinhaConta() {
  const { path } = useLocation();

  return (
    <section class="pagina">
      <h1>Minha conta</h1>

      <nav class="abas" aria-label="Seções da conta">
        {secoes.map((secao) => (
          <a
            key={secao.caminho}
            href={secao.caminho}
            class={ativa(path, secao.caminho) ? 'aba aba--ativa' : 'aba'}
            aria-current={ativa(path, secao.caminho) ? 'page' : undefined}
          >
            {secao.rotulo}
          </a>
        ))}
      </nav>

      <Secao caminho={path} />
    </section>
  );
}
