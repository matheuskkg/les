import { useState } from 'preact/hooks';
import type { components } from '../api/esquema';
import { enderecoVazio, type EnderecoDoFormulario } from '../cadastroDeCliente';
import { ehCadastrado, resumoDoEndereco, type EscolhaDeEndereco } from '../escolhaDeEndereco';
import { CamposDoEndereco } from './CamposDoEndereco';

type EnderecoCadastrado = components['schemas']['EnderecoDaResposta'];

interface Props {
  legenda: string;
  /** Nome do grupo de opções e prefixo dos ids dos campos. */
  prefixo: string;
  enderecos: EnderecoCadastrado[];
  escolha: EscolhaDeEndereco | null;
  /** Nulo enquanto o cliente preenche um endereço novo ainda não confirmado. */
  aoEscolher: (escolha: EscolhaDeEndereco | null) => void;
  /** Confirma o endereço novo; com `salvar`, ele entra no cadastro antes de ser usado. Devolve se deu certo. */
  aoUsarNovo: (formulario: EnderecoDoFormulario, salvar: boolean) => Promise<boolean>;
}

/**
 * Escolha de um endereço na compra (RF-VEN-005, DEC-10): um dos cadastrados ou outro, informado na hora, com a opção
 * de incorporá-lo ao cadastro.
 */
export function SelecaoDeEndereco({ legenda, prefixo, enderecos, escolha, aoEscolher, aoUsarNovo }: Props) {
  const [outro, definirOutro] = useState(false);
  const [formulario, definirFormulario] = useState<EnderecoDoFormulario>(enderecoVazio);
  const [salvar, definirSalvar] = useState(false);
  const [enviando, definirEnviando] = useState(false);

  async function usar() {
    definirEnviando(true);
    try {
      if ((await aoUsarNovo(formulario, salvar)) && salvar) {
        definirOutro(false);
        definirFormulario(enderecoVazio());
        definirSalvar(false);
      }
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <fieldset class="grupo-de-categorias escolha-de-endereco">
      <legend>{legenda}</legend>
      {enderecos.map((endereco) => (
        <label class="categoria-opcao" key={endereco.id}>
          <input
            type="radio"
            name={prefixo}
            checked={!outro && ehCadastrado(escolha, endereco.id)}
            onChange={() => {
              definirOutro(false);
              aoEscolher({ enderecoId: endereco.id });
            }}
          />
          <span>
            <strong>{endereco.nomeCurto}</strong> — {resumoDoEndereco(endereco)}
          </span>
        </label>
      ))}
      <label class="categoria-opcao">
        <input
          type="radio"
          name={prefixo}
          checked={outro}
          onChange={() => {
            definirOutro(true);
            aoEscolher(null);
          }}
        />
        Outro endereço
      </label>
      {outro && (
        <div class="escolha-de-endereco__novo">
          <CamposDoEndereco
            prefixo={prefixo}
            formulario={formulario}
            aoMudar={(campo, valor) => definirFormulario((atual) => ({ ...atual, [campo]: valor }))}
          />
          <label class="categoria-opcao">
            <input type="checkbox" checked={salvar} onChange={() => definirSalvar(!salvar)} />
            Salvar no meu cadastro
          </label>
          <div class="acoes">
            <button class="botao botao--secundario" type="button" disabled={enviando} onClick={() => void usar()}>
              {enviando ? 'Confirmando…' : 'Usar este endereço'}
            </button>
          </div>
          {escolha !== null && 'novo' in escolha && (
            <p class="campo__ajuda">Usando o endereço informado: {resumoDoEndereco(escolha.novo)}.</p>
          )}
        </div>
      )}
    </fieldset>
  );
}
