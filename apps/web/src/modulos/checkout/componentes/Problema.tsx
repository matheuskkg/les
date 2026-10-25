import type { ProblemaDetalhe } from '../../../api/cliente';
import { Erro } from '../../../ui/Erro';
import { Violacoes } from '../../../ui/Violacoes';

/** Violações de validação em lista; os demais problemas da API como mensagem de erro. */
export function Problema({ problema }: { problema: ProblemaDetalhe | null }) {
  return problema?.violacoes && problema.violacoes.length > 0 ? (
    <Violacoes violacoes={problema.violacoes} />
  ) : (
    <Erro problema={problema} />
  );
}
