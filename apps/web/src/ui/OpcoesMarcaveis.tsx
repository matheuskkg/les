/** Grupo de caixas de um filtro, combinadas com OU. */
export function OpcoesMarcaveis<T extends string>({
  legenda,
  nome,
  opcoes,
  marcados,
  alternar,
}: {
  legenda: string;
  nome: string;
  opcoes: readonly { valor: T; rotulo: string }[];
  marcados: T[];
  alternar: (valor: T) => void;
}) {
  return (
    <fieldset class="grupo-de-categorias">
      <legend>{legenda}</legend>
      <div class="categorias">
        {opcoes.map((opcao) => (
          <label class="categoria-opcao" key={opcao.valor}>
            <input
              type="checkbox"
              name={nome}
              value={opcao.valor}
              checked={marcados.includes(opcao.valor)}
              onChange={() => alternar(opcao.valor)}
            />
            {opcao.rotulo}
          </label>
        ))}
      </div>
    </fieldset>
  );
}
