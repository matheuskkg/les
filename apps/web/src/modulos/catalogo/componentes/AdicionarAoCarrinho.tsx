import { useState } from 'preact/hooks';
import type { ProblemaDetalhe } from '../../../api/cliente';
import { useCarrinho } from '../../carrinho';
import { useSessao } from '../../sessao';
import { Botao } from '../../../ui/Botao';
import { Erro } from '../../../ui/Erro';
import { SeletorDeQuantidade } from '../../../ui/SeletorDeQuantidade';
import { Violacoes } from '../../../ui/Violacoes';

interface Props {
  livroId: number;
  disponivel: number;
}

/**
 * Adiciona o livro ao carrinho (RF-VEN-001); a confirmação aparece como aviso flutuante. O visitante vai ao login
 * e, ao voltar, o livro já entra no carrinho (DEC-15.4); o administrador não tem carrinho.
 */
export function AdicionarAoCarrinho({ livroId, disponivel }: Props) {
  const { usuario } = useSessao();
  const { adicionar, entrarParaComprar } = useCarrinho();
  const [quantidade, definirQuantidade] = useState(1);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [enviando, definirEnviando] = useState(false);

  if (usuario?.papel === 'ADMIN') {
    return null;
  }

  async function enviar(evento: Event) {
    evento.preventDefault();
    if (usuario === null) {
      entrarParaComprar({ livroId, quantidade });
      return;
    }
    definirEnviando(true);
    definirProblema(null);
    definirProblema(await adicionar(livroId, quantidade));
    definirEnviando(false);
  }

  return (
    <form class="adicionar-ao-carrinho" onSubmit={(evento) => void enviar(evento)}>
      <SeletorDeQuantidade
        id={`quantidade-${livroId}`}
        valor={quantidade}
        maximo={Math.max(1, disponivel)}
        aoMudar={definirQuantidade}
      />
      <Botao type="submit" disabled={enviando}>
        {enviando ? 'Adicionando…' : 'Adicionar ao carrinho'}
      </Botao>
      {problema?.violacoes && problema.violacoes.length > 0 ? (
        <Violacoes violacoes={problema.violacoes} />
      ) : (
        <Erro problema={problema} />
      )}
    </form>
  );
}
