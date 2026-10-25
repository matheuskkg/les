import { etapasDoPedido, type StatusDoPedido } from '../regras/linhaDoTempo';

/** Linha do tempo do pedido (RF-VEN-009, RF-VEN-010), da realização à entrega. */
export function LinhaDoTempo({ status }: { status: StatusDoPedido }) {
  return (
    <ol class="linha-do-tempo" aria-label="Andamento do pedido">
      {etapasDoPedido(status).map((etapa) => (
        <li
          key={etapa.rotulo}
          class={`linha-do-tempo__etapa linha-do-tempo__etapa--${etapa.situacao}`}
          aria-current={etapa.situacao === 'atual' ? 'step' : undefined}
        >
          <span class="linha-do-tempo__marca" aria-hidden="true" />
          {etapa.rotulo}
        </li>
      ))}
    </ol>
  );
}
