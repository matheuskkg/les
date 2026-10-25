/** Tempo que falta para o prazo de reserva dos itens (RNF-VEN-001), pronto para exibir. */
export interface TempoRestante {
  /** Milissegundos até o prazo; zero quando já passou. */
  restante: number;
  /** Relógio "m:ss", ou "h:mm:ss" a partir de uma hora. */
  texto: string;
  /** Menos de cinco minutos: a tela destaca o aviso. */
  acabando: boolean;
}

const CINCO_MINUTOS = 5 * 60_000;

export function tempoRestante(expiraEm: string, agora: number): TempoRestante {
  const restante = Math.max(0, Date.parse(expiraEm) - agora);
  const totalDeSegundos = Math.floor(restante / 1000);
  const horas = Math.floor(totalDeSegundos / 3600);
  const minutos = Math.floor((totalDeSegundos % 3600) / 60);
  const segundos = totalDeSegundos % 60;
  const ss = String(segundos).padStart(2, '0');
  const texto = horas > 0 ? `${horas}:${String(minutos).padStart(2, '0')}:${ss}` : `${minutos}:${ss}`;
  return { restante, texto, acabando: restante < CINCO_MINUTOS };
}
