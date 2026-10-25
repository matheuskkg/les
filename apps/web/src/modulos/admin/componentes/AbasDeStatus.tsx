import type { components } from '../../../api/esquema';
import { STATUS, type StatusDoPedido } from '../regras/filtrosDePedidos';

type Contagem = components['schemas']['ContagemPorStatusDto'];

interface Props {
  contagem: Contagem | null;
  /** Status filtrados agora; a aba fica ativa quando é só ele (ou nenhum, para "Todos"). */
  marcados: readonly StatusDoPedido[];
  urlDoStatus: (status: StatusDoPedido | null) => string;
}

/** Abas por status do pedido com a quantidade de cada um (RF-VEN-009), vinda de `/api/admin/pedidos/contagem`. */
export function AbasDeStatus({ contagem, marcados, urlDoStatus }: Props) {
  const total = contagem ? Object.values(contagem.pedidos).reduce((soma, quantidade) => soma + quantidade, 0) : null;
  const abas: { status: StatusDoPedido | null; rotulo: string; quantidade: number | null }[] = [
    { status: null, rotulo: 'Todos', quantidade: total },
    ...STATUS.map(({ valor, rotulo }) => ({ status: valor, rotulo, quantidade: contagem?.pedidos[valor] ?? null })),
  ];

  return (
    <nav class="abas" aria-label="Pedidos por status">
      {abas.map(({ status, rotulo, quantidade }) => {
        const ativa = status === null ? marcados.length === 0 : marcados.length === 1 && marcados[0] === status;
        return (
          <a
            key={rotulo}
            href={urlDoStatus(status)}
            class={ativa ? 'aba aba--ativa' : 'aba'}
            aria-current={ativa ? 'page' : undefined}
          >
            {rotulo}
            {quantidade !== null && <span class="aba__total">{quantidade}</span>}
          </a>
        );
      })}
    </nav>
  );
}
