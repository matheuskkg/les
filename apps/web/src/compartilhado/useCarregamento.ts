import { useCallback, useEffect, useRef, useState } from 'preact/hooks';
import { interpretarProblema, type ProblemaDetalhe } from '../api/cliente';

/** Resposta do openapi-fetch reduzida ao que o hook usa. */
export interface RespostaDaApi<T> {
  data?: T;
  error?: unknown;
  response: Response;
}

export interface Carregamento<T> {
  /** Último dado recebido; nulo antes da primeira resposta e quando ela falha. */
  dados: T | null;
  problema: ProblemaDetalhe | null;
  carregando: boolean;
  /** Busca de novo sem apagar o que já está na tela. */
  recarregar: () => void;
}

const FALHA_DE_CONEXAO = 'Não foi possível conectar ao servidor. Tente novamente.';

/**
 * Carrega dados da API para uma tela (DEC-17): cancela a busca anterior quando as dependências mudam, descarta
 * respostas atrasadas e expõe carregando e problema para a tela mostrar o esqueleto ou o erro.
 */
export function useCarregamento<T>(
  buscar: (sinal: AbortSignal) => Promise<RespostaDaApi<T>>,
  dependencias: readonly unknown[],
): Carregamento<T> {
  const [dados, definirDados] = useState<T | null>(null);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [carregando, definirCarregando] = useState(true);
  const [versao, definirVersao] = useState(0);
  const buscarAtual = useRef(buscar);
  buscarAtual.current = buscar;

  useEffect(() => {
    const controle = new AbortController();
    definirCarregando(true);

    buscarAtual
      .current(controle.signal)
      .then(({ data, error, response }) => {
        if (controle.signal.aborted) {
          return;
        }
        if (!response.ok || data === undefined) {
          definirDados(null);
          definirProblema(interpretarProblema(error, response.status));
        } else {
          definirDados(data);
          definirProblema(null);
        }
        definirCarregando(false);
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirDados(null);
          definirProblema({ status: 0, detail: FALHA_DE_CONEXAO });
          definirCarregando(false);
        }
      });

    return () => controle.abort();
  }, [...dependencias, versao]);

  const recarregar = useCallback(() => definirVersao((atual) => atual + 1), []);

  return { dados, problema, carregando, recarregar };
}
