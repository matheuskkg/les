import { formatarCentavos } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { CamposDoCartao } from '../../conta';
import { NOVO, type PagamentoDaCompra } from '../hooks/usePagamentoDaCompra';
import { avisoDoValorDoCartao } from '../regras/pagamentoDaCompra';

/**
 * Passo dos cartões (RF-VEN-006, RN-VEN-004, RN-VEN-005, DEC-11): cadastrados ou um novo, cada um com o valor e o
 * código de segurança. O campo de valor aponta o mínimo por cartão e a legenda mostra o que falta no conjunto,
 * independentemente da ordem de escolha.
 */
export function CartoesDaCompra({ pagamento }: { pagamento: PagamentoDaCompra }) {
  const { resumo, cartoes, emUso, alternarCartao, digitado, digitar, faltaPagar, excedente, comCupom } =
    pagamento;

  if (resumo === null) {
    return <p class="campo__ajuda">Escolha o endereço de entrega para ver quanto falta pagar.</p>;
  }
  if (resumo.restante === '0.00') {
    return <p class="aviso aviso--sucesso">Os cupons cobrem o total: não é preciso cartão.</p>;
  }

  const restante = resumo.restante;

  function camposDoPagamento(chave: string) {
    return (
      <div class="grade-do-cadastro">
        <CampoDeFormulario
          id={`cartao-${chave}-valor`}
          rotulo="Valor neste cartão (R$)"
          inputMode="decimal"
          placeholder="0,00"
          required
          valor={digitado(chave).valor}
          erro={
            avisoDoValorDoCartao(
              digitado(chave).valor,
              restante,
              comCupom,
              emUso.length,
            ) ?? undefined
          }
          aoMudar={(valor) => digitar(chave, 'valor', valor)}
        />
        <CampoDeFormulario
          id={`cartao-${chave}-cvv`}
          rotulo="Código de segurança"
          mascara="cvv"
          placeholder="123"
          required
          autoComplete="cc-csc"
          valor={digitado(chave).cvv}
          aoMudar={(valor) => digitar(chave, 'cvv', valor)}
        />
      </div>
    );
  }

  return (
    <fieldset class="grupo-de-categorias escolha-de-endereco">
      <legend class={excedente > 0 ? 'cartoes__legenda cartoes__legenda--erro' : 'cartoes__legenda'}>
        Cartões —{' '}
        {excedente > 0
          ? `passou ${formatarCentavos(excedente)} do total`
          : `falta pagar ${formatarCentavos(faltaPagar ?? 0)}`}
      </legend>
      {excedente > 0 && (
        <p class="campo__erro" role="alert">
          Os cartões passam {formatarCentavos(excedente)} do total.
        </p>
      )}
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
        Mínimo de R$ 10,00 por cartão. Com cupom e menos de R$ 10,00 a pagar, use um único cartão para o restante. O
        código de segurança não fica gravado.
      </p>
    </fieldset>
  );
}
