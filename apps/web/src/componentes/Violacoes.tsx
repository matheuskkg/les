import type { Violacao } from '../api/cliente';

interface Props {
  violacoes?: Violacao[] | null;
}

/** Lista as violações de regra de negócio de um 422, com o código do requisito. */
export function Violacoes({ violacoes }: Props) {
  if (!violacoes || violacoes.length === 0) {
    return null;
  }

  return (
    <ul class="violacoes" role="alert">
      {violacoes.map((violacao) => (
        <li key={`${violacao.codigo}-${violacao.mensagem}`}>
          <span class="violacoes__codigo">{violacao.codigo}</span>
          <span>{violacao.mensagem}</span>
        </li>
      ))}
    </ul>
  );
}
