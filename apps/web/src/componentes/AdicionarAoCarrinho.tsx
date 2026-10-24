import { useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import { useSessao } from '../contextoDeSessao';
import { Erro } from './Erro';
import { Violacoes } from './Violacoes';

interface Props {
  livroId: number;
  disponivel: number;
}

/**
 * Adiciona o livro ao carrinho (RF-VEN-001). Visitante vai ao login e volta para o livro (DEC-15.4); o
 * administrador não tem carrinho.
 */
export function AdicionarAoCarrinho({ livroId, disponivel }: Props) {
  const { usuario } = useSessao();
  const { route, path } = useLocation();
  const [quantidade, definirQuantidade] = useState('1');
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [adicionado, definirAdicionado] = useState(false);
  const [enviando, definirEnviando] = useState(false);

  if (usuario?.papel === 'ADMIN') {
    return null;
  }

  async function adicionar(evento: Event) {
    evento.preventDefault();
    if (usuario === null) {
      route(`/login?destino=${encodeURIComponent(path)}`);
      return;
    }
    const unidades = Number.parseInt(quantidade, 10);
    if (!Number.isSafeInteger(unidades) || unidades < 1) {
      definirProblema({ status: 0, detail: 'Informe uma quantidade a partir de 1.' });
      return;
    }
    definirEnviando(true);
    definirProblema(null);
    definirAdicionado(false);
    try {
      const { error, response } = await cliente.POST('/api/me/carrinho/itens', {
        body: { livroId, quantidade: unidades },
      });
      if (!response.ok) {
        definirProblema(interpretarProblema(error, response.status));
        return;
      }
      definirAdicionado(true);
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível conectar ao servidor. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <form class="adicionar-ao-carrinho" onSubmit={(evento) => void adicionar(evento)}>
      <label class="adicionar-ao-carrinho__quantidade">
        Quantidade
        <input
          class="campo__entrada"
          type="number"
          min={1}
          max={disponivel}
          value={quantidade}
          onInput={(evento) => definirQuantidade(evento.currentTarget.value)}
        />
      </label>
      <button class="botao" type="submit" disabled={enviando}>
        {enviando ? 'Adicionando…' : 'Adicionar ao carrinho'}
      </button>
      {adicionado && (
        <p class="aviso" role="status">
          Adicionado ao carrinho. <a href="/carrinho">Ver carrinho</a>
        </p>
      )}
      {problema?.violacoes && problema.violacoes.length > 0 ? (
        <Violacoes violacoes={problema.violacoes} />
      ) : (
        <Erro problema={problema} />
      )}
    </form>
  );
}
