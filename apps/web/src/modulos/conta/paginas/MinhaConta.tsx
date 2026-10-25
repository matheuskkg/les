import { useLocation } from 'preact-iso';
import { cliente } from '../../../api/cliente';
import { formatarData, formatarDataHora, formatarDinheiro } from '../../../compartilhado/formatacao';
import { useCarregamento } from '../../../compartilhado/useCarregamento';
import { BotaoLink } from '../../../ui/Botao';
import { Esqueleto } from '../../../ui/Esqueleto';
import { EtiquetaDeStatus } from '../../../ui/EtiquetaDeStatus';
import { Painel } from '../../../ui/Painel';
import { LinhaDoTempo } from '../../pedidos';
import { useSessao } from '../../sessao';
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

function UltimoPedido() {
  const { dados, carregando } = useCarregamento(
    (signal) =>
      cliente.GET('/api/me/pedidos', { params: { query: { tamanho: 1, ordem: ['realizadoEm,desc'] } }, signal }),
    [],
  );
  const pedido = dados?.itens[0];

  return (
    <Painel titulo="Último pedido" acoes={<a href="/minha-conta/pedidos">Ver todos</a>}>
      {carregando ? (
        <Esqueleto linhas={2} rotulo="Carregando o último pedido…" />
      ) : pedido === undefined ? (
        <>
          <p>Você ainda não fez nenhum pedido.</p>
          <BotaoLink href="/">Ver livros</BotaoLink>
        </>
      ) : (
        <>
          <p class="ultimo-pedido">
            <a href={`/minha-conta/pedidos/${pedido.id}`}>
              <strong>Pedido {pedido.id}</strong>
            </a>{' '}
            · {formatarDataHora(pedido.realizadoEm)} · {formatarDinheiro(pedido.total)}{' '}
            <EtiquetaDeStatus status={pedido.status} />
          </p>
          <LinhaDoTempo status={pedido.status} />
        </>
      )}
    </Painel>
  );
}

function CuponsDeTroca() {
  const { dados, carregando } = useCarregamento((signal) => cliente.GET('/api/me/cupons-de-troca', { signal }), []);

  return (
    <Painel titulo="Cupons de troca">
      {carregando ? (
        <Esqueleto linhas={1} rotulo="Carregando os cupons…" />
      ) : !dados || dados.length === 0 ? (
        <p class="campo__ajuda">Nenhum cupom disponível. Cupons de troca aparecem aqui quando uma troca é concluída.</p>
      ) : (
        <ul class="itens-da-conta">
          {dados.map((cupom) => (
            <li key={cupom.id} class="item-da-conta">
              <strong>{cupom.codigo}</strong>
              <span>
                {formatarDinheiro(cupom.valor)}
                {cupom.validade && ` · vale até ${formatarData(cupom.validade)}`}
              </span>
            </li>
          ))}
        </ul>
      )}
    </Painel>
  );
}

/** Início da área do cliente: o andamento do último pedido e os cupons para usar. */
function Resumo() {
  const { usuario } = useSessao();
  return (
    <div class="resumo-da-conta">
      <p class="resumo-da-conta__saudacao">Olá, {usuario?.nome}.</p>
      <UltimoPedido />
      <CuponsDeTroca />
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
