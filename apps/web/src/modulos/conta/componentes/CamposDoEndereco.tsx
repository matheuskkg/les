import {
  TIPOS_DE_ENDERECO,
  TIPOS_DE_LOGRADOURO,
  UFS,
  type EnderecoDoFormulario,
} from '../regras/cadastroDeCliente';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { CampoDeSelecao } from '../../../ui/CampoDeSelecao';

interface Props {
  /** Prefixo dos ids dos campos, para mais de um endereço na mesma tela. */
  prefixo: string;
  formulario: EnderecoDoFormulario;
  aoMudar: (campo: keyof EnderecoDoFormulario, valor: string) => void;
}

/** Campos de um endereço (RN-CLI-003, DEC-10), usados em "Minha conta" e no checkout. O país é sempre o Brasil. */
export function CamposDoEndereco({ prefixo, formulario, aoMudar }: Props) {
  return (
    <>
      <div class="grade-do-cadastro">
        <CampoDeFormulario id={`${prefixo}-nome`} rotulo="Nome curto" required valor={formulario.nomeCurto}
          aoMudar={(valor) => aoMudar('nomeCurto', valor)} placeholder="Ex.: Casa da praia" />
        <CampoDeSelecao id={`${prefixo}-tipo`} rotulo="Tipo" required valor={formulario.tipoEndereco}
          opcoes={TIPOS_DE_ENDERECO} placeholder="Selecione" aoMudar={(valor) => aoMudar('tipoEndereco', valor)} />
        <CampoDeSelecao id={`${prefixo}-logradouro-tipo`} rotulo="Tipo de logradouro" required
          valor={formulario.tipoLogradouro} opcoes={TIPOS_DE_LOGRADOURO} placeholder="Selecione"
          aoMudar={(valor) => aoMudar('tipoLogradouro', valor)} />
        <div class="campo--largo">
          <CampoDeFormulario id={`${prefixo}-logradouro`} rotulo="Logradouro" required valor={formulario.logradouro}
            aoMudar={(valor) => aoMudar('logradouro', valor)} />
        </div>
        <CampoDeFormulario id={`${prefixo}-numero`} rotulo="Número" required valor={formulario.numero}
          aoMudar={(valor) => aoMudar('numero', valor)} />
        <CampoDeFormulario id={`${prefixo}-bairro`} rotulo="Bairro" required valor={formulario.bairro}
          aoMudar={(valor) => aoMudar('bairro', valor)} />
        <CampoDeFormulario id={`${prefixo}-cep`} rotulo="CEP" required mascara="cep" valor={formulario.cep}
          aoMudar={(valor) => aoMudar('cep', valor)} />
        <CampoDeFormulario id={`${prefixo}-cidade`} rotulo="Cidade" required valor={formulario.cidade}
          aoMudar={(valor) => aoMudar('cidade', valor)} />
        <CampoDeSelecao id={`${prefixo}-uf`} rotulo="UF" required valor={formulario.uf} opcoes={UFS}
          placeholder="Selecione" aoMudar={(valor) => aoMudar('uf', valor)} />
        <div class="campo--largo">
          <CampoDeFormulario id={`${prefixo}-observacoes`} rotulo="Observações" valor={formulario.observacoes}
            aoMudar={(valor) => aoMudar('observacoes', valor)} />
        </div>
      </div>
      <p class="campo__ajuda">País: Brasil.</p>
    </>
  );
}
