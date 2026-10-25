import { useEffect, useState } from 'preact/hooks';

interface Props {
  id: string;
  rotulo?: string;
  valor: number;
  minimo?: number;
  maximo?: number;
  disabled?: boolean;
  /** Chamado com a nova quantidade, já dentro dos limites. */
  aoMudar: (valor: number) => void;
}

/** Quantidade com botões de menos e mais; o campo aceita digitação e aplica ao sair ou com Enter. */
export function SeletorDeQuantidade({ id, rotulo = 'Quantidade', valor, minimo = 1, maximo, disabled, aoMudar }: Props) {
  const [texto, definirTexto] = useState(String(valor));

  useEffect(() => definirTexto(String(valor)), [valor]);

  function limitar(numero: number): number {
    const acima = Math.max(minimo, numero);
    return maximo === undefined ? acima : Math.min(maximo, acima);
  }

  function aplicar() {
    const numero = Number.parseInt(texto, 10);
    if (!Number.isSafeInteger(numero)) {
      definirTexto(String(valor));
      return;
    }
    const limitado = limitar(numero);
    definirTexto(String(limitado));
    if (limitado !== valor) {
      aoMudar(limitado);
    }
  }

  return (
    <div class="quantidade">
      <label class="quantidade__rotulo" for={id}>
        {rotulo}
      </label>
      <div class="quantidade__controle">
        <button
          type="button"
          class="quantidade__passo"
          aria-label="Uma unidade a menos"
          disabled={disabled || valor <= minimo}
          onClick={() => aoMudar(limitar(valor - 1))}
        >
          −
        </button>
        <input
          id={id}
          class="quantidade__entrada"
          type="number"
          inputMode="numeric"
          min={minimo}
          max={maximo}
          value={texto}
          disabled={disabled}
          onInput={(evento) => definirTexto(evento.currentTarget.value)}
          onBlur={aplicar}
          onKeyDown={(evento) => {
            if (evento.key === 'Enter') {
              evento.preventDefault();
              aplicar();
            }
          }}
        />
        <button
          type="button"
          class="quantidade__passo"
          aria-label="Uma unidade a mais"
          disabled={disabled || (maximo !== undefined && valor >= maximo)}
          onClick={() => aoMudar(limitar(valor + 1))}
        >
          +
        </button>
      </div>
    </div>
  );
}
