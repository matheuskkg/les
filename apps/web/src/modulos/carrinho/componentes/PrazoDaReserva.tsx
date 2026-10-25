import { useAgora } from '../../../compartilhado/useAgora';
import { formatarDataHora } from '../../../compartilhado/formatacao';
import { tempoRestante } from '../regras/reserva';

/** Contagem regressiva do bloqueio dos itens (RNF-VEN-001); o prazo exato fica no título. */
export function PrazoDaReserva({ expiraEm, prefixo = 'Itens reservados por' }: { expiraEm: string; prefixo?: string }) {
  const agora = useAgora();
  const tempo = tempoRestante(expiraEm, agora);
  return (
    <span class={tempo.acabando ? 'prazo prazo--acabando' : 'prazo'} title={`Até ${formatarDataHora(expiraEm)}`}>
      {prefixo} <span class="relogio">{tempo.texto}</span>
    </span>
  );
}
