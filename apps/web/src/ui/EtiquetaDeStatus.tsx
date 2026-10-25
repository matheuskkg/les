import { textoDoStatus, tomDoStatus } from '../compartilhado/formatacao';
import { classes } from './classes';

/** Status de pedido ou de troca como pílula: o texto é o do DRS e a cor sinaliza o estado. */
export function EtiquetaDeStatus({ status }: { status: string }) {
  return <span class={classes('etiqueta', `etiqueta--${tomDoStatus(status)}`)}>{textoDoStatus(status)}</span>;
}
