import { useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../api/cliente';
import type { components } from '../../api/esquema';
import { cartaoVazio, formatarValidade, montarCartao, type CartaoDoFormulario } from '../../cadastroDeCliente';
import { CamposDoCartao } from '../../componentes/CamposDoCartao';
import { Erro } from '../../componentes/Erro';
import { Violacoes } from '../../componentes/Violacoes';

type Cartao = components['schemas']['CartaoDaResposta'];

/**
 * Cartões do cliente logado: vários cartões, um preferencial, número sempre mascarado e sem código de segurança
 * gravado (RF-CLI-007, RN-CLI-004, RN-CLI-005, DEC-06, DEC-10).
 */
export function Cartoes() {
  const [cartoes, definirCartoes] = useState<Cartao[] | null>(null);
  const [novo, definirNovo] = useState<CartaoDoFormulario | null>(null);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [enviando, definirEnviando] = useState(false);

  async function carregar() {
    const { data, error, response } = await cliente.GET('/api/me/cartoes');
    if (!data) {
      definirProblema(interpretarProblema(error, response.status));
      return;
    }
    definirCartoes(data);
  }

  useEffect(() => {
    void carregar().catch(() =>
      definirProblema({ status: 0, detail: 'Não foi possível carregar os cartões. Tente novamente.' }),
    );
  }, []);

  async function executar(acao: () => Promise<{ error?: unknown; response: Response }>) {
    definirProblema(null);
    const { error, response } = await acao();
    if (!response.ok) {
      definirProblema(interpretarProblema(error, response.status));
      return false;
    }
    await carregar();
    return true;
  }

  async function adicionar(evento: Event) {
    evento.preventDefault();
    if (novo === null || enviando) {
      return;
    }
    definirEnviando(true);
    try {
      const corpo = montarCartao(novo);
      if (await executar(() => cliente.POST('/api/me/cartoes', { body: corpo }))) {
        definirNovo(null);
      }
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível conectar ao servidor. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  const erros =
    problema?.violacoes && problema.violacoes.length > 0 ? (
      <Violacoes violacoes={problema.violacoes} />
    ) : (
      <Erro problema={problema} />
    );

  return (
    <div class="cartao">
      <h2>Cartões de crédito</h2>
      {novo === null && erros}

      {cartoes === null ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : cartoes.length === 0 ? (
        <p>Nenhum cartão cadastrado.</p>
      ) : (
        <ul class="itens-da-conta">
          {cartoes.map((cartao) => (
            <li key={cartao.id} class="item-da-conta">
              <div>
                <strong>{cartao.bandeira.nome}</strong> {cartao.numeroMascarado}
                {cartao.preferencial && <span class="etiqueta">Preferencial</span>}
                <br />
                {cartao.nomeImpresso} · validade {formatarValidade(cartao.validade)}
              </div>
              <div class="item-da-conta__acoes">
                {!cartao.preferencial && (
                  <button class="botao botao--secundario" type="button"
                    onClick={() => void executar(() =>
                      cliente.POST('/api/me/cartoes/{id}/preferencial', { params: { path: { id: cartao.id } } }))}>
                    Tornar preferencial
                  </button>
                )}
                <button class="botao botao--secundario" type="button"
                  onClick={() => void executar(() =>
                    cliente.DELETE('/api/me/cartoes/{id}', { params: { path: { id: cartao.id } } }))}>
                  Excluir
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {novo === null ? (
        <div class="acoes">
          <button class="botao" type="button" onClick={() => definirNovo(cartaoVazio())}>
            Adicionar cartão
          </button>
        </div>
      ) : (
        <form class="formulario" noValidate onSubmit={(evento) => void adicionar(evento)}>
          <h3>Novo cartão</h3>
          {erros}
          <CamposDoCartao prefixo="cartao" formulario={novo}
            aoMudar={(campo, valor) => definirNovo({ ...novo, [campo]: valor })} />
          <p class="campo__ajuda">O código de segurança é pedido só na hora da compra e não fica gravado.</p>
          <div class="acoes">
            <button class="botao" type="submit" disabled={enviando}>
              {enviando ? 'Salvando…' : 'Salvar cartão'}
            </button>
            <button class="botao botao--secundario" type="button"
              onClick={() => {
                definirNovo(null);
                definirProblema(null);
              }}>
              Cancelar
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
