import { useCallback, useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { montarEndereco, type EnderecoDoFormulario } from '../../conta/regras/cadastroDeCliente';
import { escolhaInicial, escolhaParaApi, type EscolhaDeEndereco } from '../regras/escolhaDeEndereco';
import { SEM_CHECKOUT } from './usePagamentoDaCompra';

type EnderecoCadastrado = components['schemas']['EnderecoDaResposta'];
type FreteDto = components['schemas']['FreteDto'];

/**
 * Endereços de entrega e de cobrança (RF-VEN-005, DEC-10): cadastrados ou novos, com a opção de salvar no cadastro; a
 * cobrança vem igual à entrega. O frete é recalculado a cada troca do endereço de entrega (RF-VEN-004, DEC-05.3).
 */
export function useEntregaDaCompra(checkoutId: number | undefined, aoPerderCheckout: () => void) {
  const [enderecos, definirEnderecos] = useState<EnderecoCadastrado[] | null>(null);
  const [entrega, definirEntrega] = useState<EscolhaDeEndereco | null>(null);
  const [cobrancaIgual, definirCobrancaIgual] = useState(true);
  const [cobrancaSeparada, definirCobranca] = useState<EscolhaDeEndereco | null>(null);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [frete, definirFrete] = useState<FreteDto | null>(null);
  const [calculandoFrete, definirCalculandoFrete] = useState(false);

  const carregarEscolhaInicial = useCallback(() => {
    definirProblema(null);
    void (async () => {
      const { data, error, response } = await cliente.GET('/api/me/enderecos');
      if (!data) {
        definirProblema(interpretarProblema(error, response.status));
        return;
      }
      definirEnderecos(data);
      const inicial = escolhaInicial(data);
      definirEntrega(inicial);
      definirCobranca(inicial);
    })().catch(() =>
      definirProblema({ status: 0, detail: 'Não foi possível carregar os endereços. Tente novamente.' }),
    );
  }, []);

  useEffect(() => {
    if (checkoutId !== undefined) {
      carregarEscolhaInicial();
    }
  }, [checkoutId, carregarEscolhaInicial]);

  // RF-VEN-004: o frete depende só do endereço de entrega e dos itens do checkout. O anterior fica na tela até o novo
  // chegar, para a troca de endereço não esvaziar o resumo.
  useEffect(() => {
    if (checkoutId === undefined || entrega === null) {
      definirFrete(null);
      definirCalculandoFrete(false);
      definirProblema(null);
      return;
    }
    let ativo = true;
    definirCalculandoFrete(true);
    void (async () => {
      const { data, error, response } = await cliente.POST('/api/me/checkout/frete', {
        body: { entrega: escolhaParaApi(entrega) },
      });
      if (!ativo) {
        return;
      }
      definirCalculandoFrete(false);
      if (!data) {
        definirFrete(null);
        const lido = interpretarProblema(error, response.status);
        if (lido.status === 404 && lido.detail === SEM_CHECKOUT) {
          // O checkout acabou (expirou ou foi encerrado): a tela passa a mostrar que não há compra em andamento.
          aoPerderCheckout();
          return;
        }
        definirProblema(lido);
        return;
      }
      definirProblema(null);
      definirFrete(data);
    })().catch(() => {
      if (ativo) {
        definirCalculandoFrete(false);
        definirFrete(null);
        definirProblema({ status: 0, detail: 'Não foi possível calcular o frete. Tente novamente.' });
      }
    });
    return () => {
      ativo = false;
    };
  }, [checkoutId, entrega, aoPerderCheckout]);

  /** Usa um endereço novo; com `salvar`, grava no cadastro (RF-CLI-006) e passa a usá-lo pelo id. */
  async function usarNovo(
    formulario: EnderecoDoFormulario,
    salvar: boolean,
    definirEscolha: (escolha: EscolhaDeEndereco) => void,
  ): Promise<boolean> {
    definirProblema(null);
    if (!salvar) {
      definirEscolha({ novo: formulario });
      return true;
    }
    try {
      const { data, error, response } = await cliente.POST('/api/me/enderecos', { body: montarEndereco(formulario) });
      if (!data) {
        definirProblema(interpretarProblema(error, response.status));
        return false;
      }
      // Já gravado: entra na lista sem nova leitura, para que uma falha dela não leve a gravar de novo.
      definirEnderecos((atuais) => [...(atuais ?? []), data]);
      definirEscolha({ enderecoId: data.id });
      return true;
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível salvar o endereço. Tente novamente.' });
      return false;
    }
  }

  function alternarCobrancaIgual() {
    // DEC-10: a cobrança separada começa igual à entrega atual.
    if (cobrancaIgual) {
      definirCobranca(entrega);
    }
    definirCobrancaIgual(!cobrancaIgual);
  }

  return {
    enderecos,
    entrega,
    definirEntrega,
    cobrancaIgual,
    alternarCobrancaIgual,
    cobrancaSeparada,
    definirCobranca,
    /** O endereço de cobrança efetivo, que vai na finalização. */
    cobranca: cobrancaIgual ? entrega : cobrancaSeparada,
    problema,
    frete,
    /** O frete está sendo recalculado; até lá, {@link frete} é o do endereço anterior. */
    calculandoFrete,
    carregarEscolhaInicial,
    usarNovoNaEntrega: (formulario: EnderecoDoFormulario, salvar: boolean) =>
      usarNovo(formulario, salvar, definirEntrega),
    usarNovoNaCobranca: (formulario: EnderecoDoFormulario, salvar: boolean) =>
      usarNovo(formulario, salvar, definirCobranca),
  };
}
