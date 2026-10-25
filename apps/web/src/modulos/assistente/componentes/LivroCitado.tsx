import { useState } from 'preact/hooks';
import type { ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { formatarDinheiro } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';
import { CapaDeLivro } from '../../../ui/CapaDeLivro';
import { Erro } from '../../../ui/Erro';
import { Violacoes } from '../../../ui/Violacoes';
import { useCarrinho } from '../../carrinho';

type LivroListado = components['schemas']['LivroListadoDto'];

/**
 * Livro citado pelo assistente (DEC-03): capa, preço, link para o detalhe e botão de adicionar ao carrinho. A IA não
 * executa ações: o carrinho só muda pelo botão. A confirmação aparece no aviso flutuante do carrinho e o erro no
 * próprio cartão.
 */
export function LivroCitado({ livro }: { livro: LivroListado }) {
  const { adicionar } = useCarrinho();
  const [adicionando, definirAdicionando] = useState(false);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const disponivel = livro.disponivel > 0 && Boolean(livro.precoVenda);

  async function adicionarAoCarrinho() {
    definirAdicionando(true);
    definirProblema(null);
    definirProblema(await adicionar(livro.id, 1));
    definirAdicionando(false);
  }

  return (
    <article class="livro-citado">
      <a href={`/livros/${livro.id}`} tabIndex={-1} aria-hidden="true">
        <CapaDeLivro
          titulo={livro.titulo}
          autores={livro.autores}
          categoria={livro.categorias[0]?.nome}
          tamanho="pequena"
        />
      </a>
      <div class="livro-citado__dados">
        <a class="livro-citado__titulo" href={`/livros/${livro.id}`}>
          {livro.titulo}
        </a>
        <span class="livro-citado__autores">{livro.autores.join('; ')}</span>
        <span class="livro-citado__preco">
          {livro.precoVenda && disponivel ? formatarDinheiro(livro.precoVenda) : 'Esgotado'}
        </span>
        {disponivel && (
          <Botao
            variante="secundario"
            class="livro-citado__adicionar"
            disabled={adicionando}
            aria-label={`Adicionar ${livro.titulo} ao carrinho`}
            onClick={() => void adicionarAoCarrinho()}
          >
            {adicionando ? 'Adicionando…' : 'Adicionar ao carrinho'}
          </Botao>
        )}
        {problema?.violacoes && problema.violacoes.length > 0 ? (
          <Violacoes violacoes={problema.violacoes} />
        ) : (
          <Erro problema={problema} />
        )}
      </div>
    </article>
  );
}
