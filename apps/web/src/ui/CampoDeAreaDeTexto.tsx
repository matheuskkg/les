interface Props {
  id: string;
  rotulo: string;
  valor: string;
  aoMudar: (valor: string) => void;
  name?: string;
  linhas?: number;
  ajuda?: string;
  erro?: string;
  placeholder?: string;
}

/** Área de texto nativa com as mesmas classes, ajuda e erro dos demais campos. */
export function CampoDeAreaDeTexto({
  id,
  rotulo,
  valor,
  aoMudar,
  name,
  linhas = 3,
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
      </label>
      <textarea
        class="campo__entrada"
        id={id}
        name={name ?? id}
        rows={linhas}
        value={valor}
        placeholder={placeholder}
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
