import type { ProblemaDetalhe } from '../api/cliente';

interface Props {
  problema?: ProblemaDetalhe | null;
  mensagem?: string | null;
}

/** Mensagem geral de erro (ProblemDetail.detail), em português e com aria-live. */
export function Erro({ problema, mensagem }: Props) {
  const texto = mensagem ?? problema?.detail;
  if (!texto) {
    return null;
  }

  return (
    <p class="erro" role="alert">
      {texto}
    </p>
  );
}
