import { ajudaDaBandeira } from '../regras/bandeiras';
import type { CartaoDoFormulario } from '../regras/cadastroDeCliente';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';

interface Props {
  /** Prefixo dos ids dos campos, para mais de um cartão na mesma tela. */
  prefixo: string;
  formulario: CartaoDoFormulario;
  aoMudar: (campo: keyof CartaoDoFormulario, valor: string) => void;
}

/** Campos de um cartão, sem o código de segurança (RN-CLI-004, RN-CLI-005, DEC-06, DEC-10, DEC-18). */
export function CamposDoCartao({ prefixo, formulario, aoMudar }: Props) {
  return (
    <div class="grade-do-cadastro">
      <CampoDeFormulario id={`${prefixo}-numero`} rotulo="Número do cartão" required mascara="numeroDoCartao"
        autoComplete="cc-number" valor={formulario.numero} placeholder="0000 0000 0000 0000"
        ajuda={ajudaDaBandeira(formulario.numero)}
        aoMudar={(valor) => aoMudar('numero', valor)} />
      <CampoDeFormulario id={`${prefixo}-nome`} rotulo="Nome impresso" required autoComplete="cc-name"
        valor={formulario.nomeImpresso} placeholder="Como impresso no cartão"
        aoMudar={(valor) => aoMudar('nomeImpresso', valor)} />
      <CampoDeFormulario id={`${prefixo}-validade`} rotulo="Validade" tipo="month" required
        valor={formulario.validade} aoMudar={(valor) => aoMudar('validade', valor)} />
    </div>
  );
}
