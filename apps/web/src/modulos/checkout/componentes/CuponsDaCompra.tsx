import { formatarData, formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import type { PagamentoDaCompra } from '../hooks/usePagamentoDaCompra';
import { Problema } from './Problema';

/** Passo dos cupons (RF-VEN-006, RN-VEN-006): um promocional pelo código e os cupons de troca do cliente. */
export function CuponsDaCompra({ pagamento }: { pagamento: PagamentoDaCompra }) {
  const { codigo, definirCodigo, promocional, aplicarPromocional, retirarPromocional, cupons, selecionados } =
    pagamento;
  return (
    <>
      <form
        class="pagamento-da-compra__promocional"
        noValidate
        onSubmit={(evento) => {
          evento.preventDefault();
          aplicarPromocional(codigo);
        }}
      >
        <CampoDeFormulario
          id="cupom-promocional"
          rotulo="Cupom promocional"
          valor={codigo}
          aoMudar={definirCodigo}
          placeholder="BEMVINDO10"
          ajuda="Um cupom promocional por compra."
        />
        <div class="acoes">
          <Botao variante="secundario" type="submit">
            Aplicar cupom
          </Botao>
          {promocional && (
            <Botao variante="secundario" onClick={retirarPromocional}>
              Retirar {promocional}
            </Botao>
          )}
        </div>
      </form>

      {cupons.length > 0 ? (
        <fieldset class="grupo-de-categorias escolha-de-endereco">
          <legend>Cupons de troca</legend>
          {cupons.map((cupom) => (
            <label class="categoria-opcao" key={cupom.id}>
              <input
                type="checkbox"
                checked={selecionados.includes(cupom.id)}
                onChange={() => pagamento.alternarCupom(cupom.id)}
              />
              <span>
                {cupom.codigo} — {formatarDinheiro(cupom.valor)}
                {cupom.validade && ` (vale até ${formatarData(cupom.validade)})`}
              </span>
            </label>
          ))}
        </fieldset>
      ) : (
        <p class="campo__ajuda">Você não tem cupons de troca.</p>
      )}
      <Problema problema={pagamento.problemaDosCupons} />
    </>
  );
}
