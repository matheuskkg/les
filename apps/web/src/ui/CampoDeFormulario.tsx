type TipoDeCampo = 'text' | 'email' | 'password' | 'tel' | 'search' | 'date' | 'month';

interface Props {
  id: string;
  rotulo: string;
  valor: string;
  aoMudar: (valor: string) => void;
  tipo?: TipoDeCampo;
  name?: string;
  autoComplete?: string;
  inputMode?: 'numeric' | 'decimal' | 'tel' | 'email';
  required?: boolean;
  disabled?: boolean;
  ajuda?: string;
  erro?: string;
  placeholder?: string;
  minLength?: number;
  maxLength?: number;
}

/** Campo de formulário nativo com rótulo, ajuda e mensagem de erro acessíveis. */
export function CampoDeFormulario({
  id,
  rotulo,
  valor,
  aoMudar,
  tipo = 'text',
  name,
  autoComplete,
  inputMode,
  required = false,
  disabled = false,
  ajuda,
  erro,
  placeholder,
  minLength,
  maxLength,
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
      <input
        class="campo__entrada"
        id={id}
        name={name ?? id}
        /* O JSX do Preact 11 tipa `type` como união discriminada; o cast evita que o
           valor dinâmico force a checagem de todos os ramos ao mesmo tempo. */
        type={tipo as 'text'}
        value={valor}
        required={required}
        disabled={disabled}
        autoComplete={autoComplete}
        inputMode={inputMode}
        placeholder={placeholder}
        minLength={minLength}
        maxLength={maxLength}
        aria-invalid={erro ? 'true' : undefined}
        aria-describedby={descricao !== '' ? descricao : undefined}
        onInput={(evento) => aoMudar(evento.currentTarget.value)}
      />
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
