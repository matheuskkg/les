export interface OpcaoDeSelecao {
  valor: string;
  rotulo: string;
}

interface Props {
  id: string;
  rotulo: string;
  valor: string;
  aoMudar: (valor: string) => void;
  opcoes: ReadonlyArray<OpcaoDeSelecao>;
  name?: string;
  required?: boolean;
  disabled?: boolean;
  ajuda?: string;
  erro?: string;
  placeholder?: string;
}

/** Seleção nativa com as mesmas classes, ajuda e erro do campo de texto. */
export function CampoDeSelecao({
  id,
  rotulo,
  valor,
  aoMudar,
  opcoes,
  name,
  required = false,
  disabled = false,
  ajuda,
  erro,
  placeholder,
}: Props) {
  const idDaAjuda = ajuda ? `${id}-ajuda` : undefined;
  const idDoErro = erro ? `${id}-erro` : undefined;
  const descricao = [idDaAjuda, idDoErro].filter((item) => item !== undefined).join(' ');

  return (
    <div class={`campo${erro ? ' campo--com-erro' : ''}`}>
      <label class="campo__rotulo" for={id}>
        {rotulo}
        {required && (
          <span class="campo__obrigatorio" aria-hidden="true">
            {' '}
            *
          </span>
        )}
      </label>
      <select
        class="campo__entrada"
        id={id}
        name={name ?? id}
        value={valor}
        required={required}
        disabled={disabled}
        aria-invalid={erro ? 'true' : undefined}
        aria-describedby={descricao !== '' ? descricao : undefined}
        onChange={(evento) => aoMudar(evento.currentTarget.value)}
      >
        {placeholder !== undefined && <option value="">{placeholder}</option>}
        {opcoes.map((opcao) => (
          <option key={opcao.valor} value={opcao.valor}>
            {opcao.rotulo}
          </option>
        ))}
      </select>
      {ajuda && (
        <p class="campo__ajuda" id={idDaAjuda}>
          {ajuda}
        </p>
      )}
      {erro && (
        <p class="campo__erro" id={idDoErro}>
          {erro}
        </p>
      )}
    </div>
  );
}
