import { createContext } from 'preact';
import type { ComponentChildren } from 'preact';
import { useCallback, useContext, useEffect, useRef, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../api/cliente';
import type { components } from '../../api/esquema';
import { useRecargaPorNotificacao } from '../notificacoes';
import { useSessao } from '../sessao';
import {
  CHAVE_DA_INTENCAO,
  lerIntencao,
  serializarIntencao,
  type IntencaoDeCompra,
} from './regras/intencaoDeCompra';

export type CarrinhoDto = components['schemas']['CarrinhoDto'];

type RespostaDoCarrinho = Promise<{ data?: CarrinhoDto; error?: unknown; response: Response }>;

interface AvisoFlutuante {
  texto: string;
  /** Link opcional ao lado do texto, como "Ver carrinho". */
  link?: { href: string; rotulo: string };
}

interface ContextoDoCarrinho {
  /** Nulo enquanto carrega ou sem sessão de cliente. */
  carrinho: CarrinhoDto | null;
  problema: ProblemaDetalhe | null;
  gavetaAberta: boolean;
  abrirGaveta: () => void;
  fecharGaveta: () => void;
  recarregar: () => Promise<void>;
  /** Adiciona e devolve o problema da API, se houver (RF-VEN-001). */
  adicionar: (livroId: number, quantidade: number) => Promise<ProblemaDetalhe | null>;
  alterar: (livroId: number, quantidade: number) => void;
  remover: (livroId: number) => void;
  dispensarRemovido: (livroId: number) => void;
  /** Guarda o livro escolhido pelo visitante e o leva ao login; volta à mesma página depois. */
  entrarParaComprar: (intencao: IntencaoDeCompra) => void;
  /** Abre o checkout com os itens (RF-VEN-003, DEC-12); com um já aberto (409), volta a ele. */
  irParaPagamento: () => Promise<void>;
  iniciandoPagamento: boolean;
  /** Encerra o checkout aberto e devolve os itens ao carrinho, somados aos que já estiverem lá (DEC-12). */
  devolverCompra: () => Promise<void>;
  devolvendoCompra: boolean;
  aviso: AvisoFlutuante | null;
  avisar: (aviso: AvisoFlutuante) => void;
}

const Contexto = createContext<ContextoDoCarrinho | null>(null);

const DURACAO_DO_AVISO = 4000;

function lerIntencaoGuardada(): IntencaoDeCompra | null {
  try {
    return lerIntencao(sessionStorage.getItem(CHAVE_DA_INTENCAO));
  } catch {
    return null;
  }
}

function guardarIntencao(intencao: IntencaoDeCompra | null): void {
  try {
    if (intencao === null) {
      sessionStorage.removeItem(CHAVE_DA_INTENCAO);
    } else {
      sessionStorage.setItem(CHAVE_DA_INTENCAO, serializarIntencao(intencao));
    }
  } catch {
    // Sem armazenamento da aba, o visitante só precisa adicionar de novo depois de entrar.
  }
}

/**
 * Carrinho do cliente logado, compartilhado pelo cabeçalho (contador e prazo), pela gaveta, pela página do
 * carrinho e pelo botão de compra do livro. Recarrega quando chega uma notificação do carrinho (DEC-13).
 */
export function CarrinhoProvider({ children }: { children: ComponentChildren }) {
  const { usuario } = useSessao();
  const { route, path } = useLocation();
  const ehCliente = usuario?.papel === 'CLIENTE';
  const [carrinho, definirCarrinho] = useState<CarrinhoDto | null>(null);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [gavetaAberta, definirGavetaAberta] = useState(false);
  const [iniciandoPagamento, definirIniciandoPagamento] = useState(false);
  const [devolvendoCompra, definirDevolvendoCompra] = useState(false);
  const [aviso, definirAviso] = useState<AvisoFlutuante | null>(null);
  const temporizador = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const avisar = useCallback((novo: AvisoFlutuante) => {
    definirAviso(novo);
    clearTimeout(temporizador.current);
    temporizador.current = setTimeout(() => definirAviso(null), DURACAO_DO_AVISO);
  }, []);

  useEffect(() => () => clearTimeout(temporizador.current), []);

  const recarregar = useCallback(async () => {
    try {
      const { data, error, response } = await cliente.GET('/api/me/carrinho');
      if (!data) {
        definirProblema(interpretarProblema(error, response.status));
        return;
      }
      definirCarrinho(data);
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível carregar o carrinho. Tente novamente.' });
    }
  }, []);

  const adicionar = useCallback(
    async (livroId: number, quantidade: number): Promise<ProblemaDetalhe | null> => {
      try {
        const { data, error, response } = await cliente.POST('/api/me/carrinho/itens', {
          body: { livroId, quantidade },
        });
        if (!response.ok || !data) {
          return interpretarProblema(error, response.status);
        }
        definirCarrinho(data);
        definirProblema(null);
        avisar({ texto: 'Adicionado ao carrinho.', link: { href: '/carrinho', rotulo: 'Ver carrinho' } });
        return null;
      } catch {
        return { status: 0, detail: 'Não foi possível conectar ao servidor. Tente novamente.' };
      }
    },
    [avisar],
  );

  // Sessão de cliente: carrega o carrinho e aplica o livro escolhido antes do login, se houver.
  useEffect(() => {
    if (!ehCliente) {
      definirCarrinho(null);
      definirProblema(null);
      definirGavetaAberta(false);
      return;
    }
    const intencao = lerIntencaoGuardada();
    guardarIntencao(null);
    if (intencao) {
      void adicionar(intencao.livroId, intencao.quantidade).then((falha) => {
        if (falha) {
          definirProblema(falha);
          void recarregar();
        }
      });
    } else {
      void recarregar();
    }
  }, [ehCliente, adicionar, recarregar]);

  // O fim do checkout também mexe no carrinho: os itens voltam como removidos (RN-VEN-014).
  const recarregarSeCliente = () => {
    if (ehCliente) {
      void recarregar();
    }
  };
  useRecargaPorNotificacao('carrinho', recarregarSeCliente);
  useRecargaPorNotificacao('checkout', recarregarSeCliente);

  async function aplicar(acao: RespostaDoCarrinho): Promise<void> {
    definirProblema(null);
    try {
      const { data, error, response } = await acao;
      if (!data) {
        definirProblema(interpretarProblema(error, response.status));
        await recarregar();
        return;
      }
      definirCarrinho(data);
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível conectar ao servidor. Tente novamente.' });
    }
  }

  async function devolverCompra() {
    definirDevolvendoCompra(true);
    try {
      await aplicar(cliente.POST('/api/me/checkout/voltar'));
    } finally {
      definirDevolvendoCompra(false);
    }
  }

  function alterar(livroId: number, quantidade: number) {
    void aplicar(
      cliente.PUT('/api/me/carrinho/itens/{livroId}', { params: { path: { livroId } }, body: { quantidade } }),
    );
  }

  function remover(livroId: number) {
    void aplicar(cliente.DELETE('/api/me/carrinho/itens/{livroId}', { params: { path: { livroId } } }));
  }

  function dispensarRemovido(livroId: number) {
    void aplicar(cliente.DELETE('/api/me/carrinho/removidos/{livroId}', { params: { path: { livroId } } }));
  }

  function entrarParaComprar(intencao: IntencaoDeCompra) {
    guardarIntencao(intencao);
    route(`/login?destino=${encodeURIComponent(path)}`);
  }

  async function irParaPagamento() {
    definirIniciandoPagamento(true);
    definirProblema(null);
    try {
      const { data, error, response } = await cliente.POST('/api/me/checkout');
      if (data || (response.status === 409 && (await cliente.GET('/api/me/checkout')).data)) {
        definirGavetaAberta(false);
        void recarregar();
        route('/checkout');
        return;
      }
      definirProblema(interpretarProblema(error, response.status));
      await recarregar();
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível ir para o pagamento. Tente novamente.' });
    } finally {
      definirIniciandoPagamento(false);
    }
  }

  return (
    <Contexto.Provider
      value={{
        carrinho,
        problema,
        gavetaAberta,
        abrirGaveta: () => definirGavetaAberta(true),
        fecharGaveta: () => definirGavetaAberta(false),
        recarregar,
        adicionar,
        alterar,
        remover,
        dispensarRemovido,
        entrarParaComprar,
        irParaPagamento: () => irParaPagamento(),
        iniciandoPagamento,
        devolverCompra,
        devolvendoCompra,
        aviso,
        avisar,
      }}
    >
      {children}
    </Contexto.Provider>
  );
}

export function useCarrinho(): ContextoDoCarrinho {
  const contexto = useContext(Contexto);
  if (!contexto) {
    throw new Error('useCarrinho precisa estar dentro de CarrinhoProvider.');
  }
  return contexto;
}
