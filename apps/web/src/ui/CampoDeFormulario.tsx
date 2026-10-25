import {
  aplicarMascara,
  apagarDigitoAntesDoSeparador,
  posicaoDoCaret,
  type Mascara,
} from '../compartilhado/mascaras';

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
  /** Máscara aplicada a cada digitação; o valor entregue a `aoMudar` já vem mascarado. */
  mascara?: Mascara;
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
  mascara,
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
        inputMode={inputMode ?? (mascara ? 'numeric' : undefined)}
        placeholder={placeholder}
        minLength={minLength}
        maxLength={maxLength}
        aria-invalid={erro ? 'true' : undefined}
        aria-describedby={descricao !== '' ? descricao : undefined}
        onInput={(evento) => {
          const campo = evento.currentTarget;
          if (mascara === undefined) {
            aoMudar(campo.value);
            return;
          }
          let bruto = campo.value;
          let caret = campo.selectionStart ?? bruto.length;
          /* Backspace logo após um separador: apaga o dígito anterior para a máscara não
             recolocar o separador e deixar o cursor preso. */
          if (evento.inputType === 'deleteContentBackward') {
            const ajuste = apagarDigitoAntesDoSeparador(valor, bruto, caret);
            if (ajuste !== null) {
              bruto = ajuste.valor;
              caret = ajuste.caret;
            }
          }
          const mascarado = aplicarMascara(mascara, bruto, valor);
          /* Se a máscara devolve o mesmo valor do estado, o Preact não re-renderiza e o
             navegador manteria o caractere digitado; por isso o DOM é corrigido aqui. */
          if (mascarado !== campo.value) {
            campo.value = mascarado;
          }
          const caretMascarado = posicaoDoCaret(bruto, caret, mascarado);
          campo.setSelectionRange(caretMascarado, caretMascarado);
          aoMudar(mascarado);
        }}
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
