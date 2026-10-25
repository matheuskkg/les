import { formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { CamposDoCartao } from '../../conta';
import { NOVO, type PagamentoDaCompra } from '../hooks/usePagamentoDaCompra';
import { avisoDoValorDoCartao } from '../regras/pagamentoDaCompra';

/**
 * Passo dos cartões (RF-VEN-006, RN-VEN-004, DEC-11): cadastrados ou um novo, cada um com o valor e o código de
 * segurança. O campo de valor já aponta o mínimo de R$ 10,00.
 */
export function CartoesDaCompra({ pagamento }: { pagamento: PagamentoDaCompra }) {
  const { resumo, cartoes, emUso, alternarCartao, digitado, digitar } = pagamento;

  if (resumo === null) {
    return <p class="campo__ajuda">Escolha o endereço de entrega para ver quanto falta pagar.</p>;
  }
  if (resumo.restante === '0.00') {
    return <p class="aviso aviso--sucesso">Os cupons cobrem o total: não é preciso cartão.</p>;
  }

  function camposDoPagamento(chave: string) {
    return (
      <div class="grade-do-cadastro">
        <CampoDeFormulario
          id={`cartao-${chave}-valor`}
          rotulo="Valor neste cartão (R$)"
          inputMode="decimal"
          required
          valor={digitado(chave).valor}
          erro={avisoDoValorDoCartao(digitado(chave).valor, resumo?.restante ?? '0.00') ?? undefined}
          aoMudar={(valor) => digitar(chave, 'valor', valor)}
        />
        <CampoDeFormulario
          id={`cartao-${chave}-cvv`}
          rotulo="Código de segurança"
          inputMode="numeric"
          required
          autoComplete="cc-csc"
          maxLength={4}
          valor={digitado(chave).cvv}
          aoMudar={(valor) => digitar(chave, 'cvv', valor)}
        />
      </div>
    );
  }

  return (
    <fieldset class="grupo-de-categorias escolha-de-endereco">
      <legend>Cartões — falta pagar {formatarDinheiro(resumo.restante)}</legend>
      {cartoes.map((cartao) => {
        const chave = String(cartao.id);
        return (
          <div key={cartao.id} class={`opcao-de-cartao${emUso.includes(chave) ? ' opcao-de-cartao--escolhida' : ''}`}>
            <label class="categoria-opcao">
              <input type="checkbox" checked={emUso.includes(chave)} onChange={() => alternarCartao(chave)} />
              <span>
                {cartao.bandeira.nome} {cartao.numeroMascarado}
                {cartao.preferencial && ' (preferencial)'}
              </span>
            </label>
            {emUso.includes(chave) && camposDoPagamento(chave)}
          </div>
        );
      })}
      <div class={`opcao-de-cartao${emUso.includes(NOVO) ? ' opcao-de-cartao--escolhida' : ''}`}>
        <label class="categoria-opcao">
          <input type="checkbox" checked={emUso.includes(NOVO)} onChange={() => alternarCartao(NOVO)} />
          Outro cartão
        </label>
        {emUso.includes(NOVO) && (
          <div class="escolha-de-endereco__novo">
            <CamposDoCartao prefixo="cartao-novo" formulario={pagamento.novo} aoMudar={pagamento.mudarNovo} />
            {camposDoPagamento(NOVO)}
            <div class="acoes">
              <Botao variante="secundario" disabled={pagamento.salvando} onClick={pagamento.salvarNovo}>
                Salvar no meu cadastro
              </Botao>
            </div>
          </div>
        )}
      </div>
      <p class="campo__ajuda">
        Mínimo de R$ 10,00 por cartão, exceto quando cupons deixam menos que isso a pagar. O código de segurança não
        fica gravado.
      </p>
    </fieldset>
  );
}
