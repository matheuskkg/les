import { abrirOpcoesPeloRotulo } from './abrirOpcoes';
import type { OpcaoDeSelecao } from './CampoDeSelecao';

interface Props {
  id: string;
  valor: string;
  aoMudar: (valor: string) => void;
  opcoes: ReadonlyArray<OpcaoDeSelecao>;
}

/** "Ordenar por" das listas (vitrine e consultas do administrador): seleção compacta ao lado do total encontrado. */
export function SeletorDeOrdem({ id, valor, aoMudar, opcoes }: Props) {
  return (
    <div class="resultado__ordem">
      <label for={id} onClick={(evento) => abrirOpcoesPeloRotulo(evento, id)}>
        Ordenar por
      </label>
      <select
        id={id}
        class="campo__entrada campo__entrada--compacta"
        value={valor}
        onChange={(evento) => aoMudar(evento.currentTarget.value)}
      >
        {opcoes.map((opcao) => (
          <option key={opcao.valor} value={opcao.valor}>
            {opcao.rotulo}
          </option>
        ))}
      </select>
    </div>
  );
}
