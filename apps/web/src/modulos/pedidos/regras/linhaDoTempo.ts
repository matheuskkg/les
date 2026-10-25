import type { components } from '../../../api/esquema';

export type StatusDoPedido = components['schemas']['PedidoDetalheDto']['status'];

export type SituacaoDaEtapa = 'concluida' | 'atual' | 'pendente' | 'falhou';

export interface EtapaDoPedido {
  rotulo: string;
  situacao: SituacaoDaEtapa;
}

const CAMINHO: readonly { status: StatusDoPedido; rotulo: string }[] = [
  { status: 'EM_PROCESSAMENTO', rotulo: 'Em processamento' },
  { status: 'APROVADA', rotulo: 'Pagamento aprovado' },
  { status: 'EM_TRANSPORTE', rotulo: 'Em transporte' },
  { status: 'ENTREGUE', rotulo: 'Entregue' },
];

/**
 * Etapas do pedido para a linha do tempo (RF-VEN-009, RF-VEN-010): o caminho EM PROCESSAMENTO → APROVADA → EM
 * TRANSPORTE → ENTREGUE, com a etapa do status atual marcada. Reprovado, o caminho para no pagamento.
 */
export function etapasDoPedido(status: StatusDoPedido): EtapaDoPedido[] {
  if (status === 'REPROVADA') {
    return [
      { rotulo: 'Em processamento', situacao: 'concluida' },
      { rotulo: 'Pagamento reprovado', situacao: 'falhou' },
    ];
  }
  const atual = CAMINHO.findIndex((etapa) => etapa.status === status);
  return CAMINHO.map((etapa, indice) => ({
    rotulo: etapa.rotulo,
    situacao:
      indice < atual || (indice === atual && status === 'ENTREGUE')
        ? 'concluida'
        : indice === atual
          ? 'atual'
          : 'pendente',
  }));
}
