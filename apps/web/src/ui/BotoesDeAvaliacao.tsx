import { classes } from './classes';

export type Avaliacao = 'POSITIVA' | 'NEGATIVA';

interface Props {
  /** Última avaliação dada, se houver; o botão correspondente aparece marcado. */
  avaliacao?: Avaliacao | null;
  /** Do que se trata, para o leitor de tela: "Dom Casmurro", "esta resposta". */
  sobre: string;
  desabilitado?: boolean;
  aoAvaliar: (avaliacao: Avaliacao) => void;
}

/** Par de botões "gostei" e "não gostei" (RNF-REC-001); a última avaliação vale. */
export function BotoesDeAvaliacao({ avaliacao, sobre, desabilitado, aoAvaliar }: Props) {
  return (
    <div class="avaliacao" role="group" aria-label={`Avaliar ${sobre}`}>
      <button
        type="button"
        class={classes('avaliacao__botao', avaliacao === 'POSITIVA' && 'avaliacao__botao--marcado')}
        aria-pressed={avaliacao === 'POSITIVA'}
        aria-label={`Gostei de ${sobre}`}
        title="Gostei"
        disabled={desabilitado}
        onClick={() => aoAvaliar('POSITIVA')}
      >
        <span aria-hidden="true">👍</span>
      </button>
      <button
        type="button"
        class={classes('avaliacao__botao', avaliacao === 'NEGATIVA' && 'avaliacao__botao--marcado')}
        aria-pressed={avaliacao === 'NEGATIVA'}
        aria-label={`Não gostei de ${sobre}`}
        title="Não gostei"
        disabled={desabilitado}
        onClick={() => aoAvaliar('NEGATIVA')}
      >
        <span aria-hidden="true">👎</span>
      </button>
    </div>
  );
}
