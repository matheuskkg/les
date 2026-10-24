import { BANDEIRAS } from '../bandeiras';
import type { CartaoDoFormulario } from '../cadastroDeCliente';
import { CampoDeFormulario } from './CampoDeFormulario';
import { CampoDeSelecao } from './CampoDeSelecao';

const OPCOES_DE_BANDEIRA = BANDEIRAS.map((bandeira) => ({ valor: String(bandeira.id), rotulo: bandeira.nome }));

interface Props {
  /** Prefixo dos ids dos campos, para mais de um cartão na mesma tela. */
  prefixo: string;
  formulario: CartaoDoFormulario;
  aoMudar: (campo: keyof CartaoDoFormulario, valor: string) => void;
}

/** Campos de um cartão, sem o código de segurança (RN-CLI-004, RN-CLI-005, DEC-06, DEC-10). */
export function CamposDoCartao({ prefixo, formulario, aoMudar }: Props) {
  return (
    <div class="grade-do-cadastro">
      <CampoDeFormulario id={`${prefixo}-numero`} rotulo="Número do cartão" required inputMode="numeric"
        autoComplete="cc-number" valor={formulario.numero} aoMudar={(valor) => aoMudar('numero', valor)} />
      <CampoDeFormulario id={`${prefixo}-nome`} rotulo="Nome impresso" required autoComplete="cc-name"
        valor={formulario.nomeImpresso} aoMudar={(valor) => aoMudar('nomeImpresso', valor)} />
      <CampoDeSelecao id={`${prefixo}-bandeira`} rotulo="Bandeira" required valor={formulario.bandeiraId}
        opcoes={OPCOES_DE_BANDEIRA} placeholder="Selecione" aoMudar={(valor) => aoMudar('bandeiraId', valor)} />
      <CampoDeFormulario id={`${prefixo}-validade`} rotulo="Validade" tipo="month" required
        valor={formulario.validade} aoMudar={(valor) => aoMudar('validade', valor)} />
    </div>
  );
}
