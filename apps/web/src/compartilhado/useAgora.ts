import { useEffect, useState } from 'preact/hooks';

/** Instante atual, atualizado a cada `intervalo` ms enquanto `ativo`; usado pelos relógios de prazo. */
export function useAgora(ativo = true, intervalo = 1000): number {
  const [agora, definirAgora] = useState(() => Date.now());

  useEffect(() => {
    if (!ativo) {
      return;
    }
    definirAgora(Date.now());
    const relogio = setInterval(() => definirAgora(Date.now()), intervalo);
    return () => clearInterval(relogio);
  }, [ativo, intervalo]);

  return agora;
}
