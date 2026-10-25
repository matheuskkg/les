import { useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../../api/cliente';
import type { components } from '../../../../api/esquema';
import {
  TIPOS_DE_ENDERECO,
  enderecoVazio,
  montarEndereco,
  type EnderecoDoFormulario,
} from '../../regras/cadastroDeCliente';
import { aplicarMascara } from '../../../../compartilhado/mascaras';
import { formatarCep } from '../../../../compartilhado/formatacao';
import { CamposDoEndereco } from '../../componentes/CamposDoEndereco';
import { Erro } from '../../../../ui/Erro';
import { Violacoes } from '../../../../ui/Violacoes';
import { Botao } from '../../../../ui/Botao';

type Endereco = components['schemas']['EnderecoDaResposta'];

/** Edição em andamento: um endereço novo (`id` nulo) ou um existente. */
interface Edicao {
  id: number | null;
  formulario: EnderecoDoFormulario;
}

function paraFormulario(endereco: Endereco): EnderecoDoFormulario {
  return {
    nomeCurto: endereco.nomeCurto,
    tipoEndereco: endereco.tipoEndereco,
    tipoLogradouro: endereco.tipoLogradouro,
    logradouro: endereco.logradouro,
    numero: endereco.numero,
    bairro: endereco.bairro,
    cep: aplicarMascara('cep', endereco.cep),
    cidade: endereco.cidade,
    uf: endereco.uf,
    observacoes: endereco.observacoes ?? '',
  };
}

function rotuloDoTipo(tipo: Endereco['tipoEndereco']): string {
  return TIPOS_DE_ENDERECO.find((opcao) => opcao.valor === tipo)?.rotulo ?? tipo;
}

/**
 * Endereços do cliente logado, alterados sem editar o restante do cadastro (RF-CLI-006, RNF-CLI-004). O único
 * endereço não pode ser excluído (RN-CLI-001, RN-CLI-002, DEC-10).
 */
export function Enderecos() {
  const [enderecos, definirEnderecos] = useState<Endereco[] | null>(null);
  const [edicao, definirEdicao] = useState<Edicao | null>(null);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [enviando, definirEnviando] = useState(false);

  async function carregar() {
    const { data, error, response } = await cliente.GET('/api/me/enderecos');
    if (!data) {
      definirProblema(interpretarProblema(error, response.status));
      return;
    }
    definirEnderecos(data);
  }

  useEffect(() => {
    void carregar().catch(() =>
      definirProblema({ status: 0, detail: 'Não foi possível carregar os endereços. Tente novamente.' }),
    );
  }, []);

  function mudar(campo: keyof EnderecoDoFormulario, valor: string) {
    definirEdicao((atual) =>
      atual === null ? atual : { ...atual, formulario: { ...atual.formulario, [campo]: valor } },
    );
  }

  async function salvar(evento: Event) {
    evento.preventDefault();
    if (edicao === null || enviando) {
      return;
    }
    definirEnviando(true);
    definirProblema(null);
    try {
      const corpo = montarEndereco(edicao.formulario);
      const { error, response } =
        edicao.id === null
          ? await cliente.POST('/api/me/enderecos', { body: corpo })
          : await cliente.PUT('/api/me/enderecos/{id}', { params: { path: { id: edicao.id } }, body: corpo });
      if (!response.ok) {
        definirProblema(interpretarProblema(error, response.status));
        return;
      }
      definirEdicao(null);
      await carregar();
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível conectar ao servidor. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  async function remover(id: number) {
    definirProblema(null);
    const { error, response } = await cliente.DELETE('/api/me/enderecos/{id}', { params: { path: { id } } });
    if (!response.ok) {
      definirProblema(interpretarProblema(error, response.status));
      return;
    }
    await carregar();
  }

  const erros =
    problema?.violacoes && problema.violacoes.length > 0 ? (
      <Violacoes violacoes={problema.violacoes} />
    ) : (
      <Erro problema={problema} />
    );

  return (
    <div class="cartao">
      <h2>Endereços</h2>
      {edicao === null && erros}

      {enderecos === null ? (
        <p class="aviso" role="status">
          Carregando…
        </p>
      ) : (
        <ul class="itens-da-conta">
          {enderecos.map((endereco) => (
            <li key={endereco.id} class="item-da-conta">
              <div>
                <strong>{endereco.nomeCurto}</strong> ({rotuloDoTipo(endereco.tipoEndereco)})
                <br />
                {endereco.logradouro}, {endereco.numero} — {endereco.bairro}, {endereco.cidade}/{endereco.uf}, CEP{' '}
                {formatarCep(endereco.cep)}
              </div>
              <div class="item-da-conta__acoes">
                <Botao variante="secundario" type="button"
                  onClick={() => definirEdicao({ id: endereco.id, formulario: paraFormulario(endereco) })}>
                  Alterar
                </Botao>
                <Botao variante="secundario" type="button" onClick={() => void remover(endereco.id)}>
                  Excluir
                </Botao>
              </div>
            </li>
          ))}
        </ul>
      )}

      {edicao === null ? (
        <div class="acoes">
          <Botao type="button" onClick={() => definirEdicao({ id: null, formulario: enderecoVazio() })}>
            Adicionar endereço
          </Botao>
        </div>
      ) : (
        <form class="formulario" noValidate onSubmit={(evento) => void salvar(evento)}>
          <h3>{edicao.id === null ? 'Novo endereço' : 'Alterar endereço'}</h3>
          {erros}
          <CamposDoEndereco prefixo="endereco" formulario={edicao.formulario} aoMudar={mudar} />
          <div class="acoes">
            <Botao type="submit" disabled={enviando}>
              {enviando ? 'Salvando…' : 'Salvar endereço'}
            </Botao>
            <Botao variante="secundario" type="button"
              onClick={() => {
                definirEdicao(null);
                definirProblema(null);
              }}>
              Cancelar
            </Botao>
          </div>
        </form>
      )}
    </div>
  );
}
