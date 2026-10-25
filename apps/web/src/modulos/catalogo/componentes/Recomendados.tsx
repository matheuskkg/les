import { useEffect, useState } from 'preact/hooks';
import { cliente } from '../../../api/cliente';
import { formatarDinheiro } from '../../../compartilhado/formatacao';
import { useCarregamento } from '../../../compartilhado/useCarregamento';
import { BotoesDeAvaliacao } from '../../../ui/BotoesDeAvaliacao';
import { CapaDeLivro } from '../../../ui/CapaDeLivro';
import { aplicarAvaliacao, type AvaliacaoDoItem, type ItemRecomendado } from '../regras/recomendados';

function CartaoRecomendado({
  item,
  avaliando,
  aoAvaliar,
}: {
  item: ItemRecomendado;
  avaliando: boolean;
  aoAvaliar: (avaliacao: AvaliacaoDoItem) => void;
}) {
  const { livro } = item;
  const autores = livro.autores.join('; ');
  return (
    <article class="livro recomendado">
      <a class="livro__capa" href={`/livros/${livro.id}`} tabIndex={-1} aria-hidden="true">
        <CapaDeLivro titulo={livro.titulo} autores={livro.autores} categoria={livro.categorias[0]?.nome} />
      </a>
      <h3 class="livro__titulo" title={livro.titulo}>
        <a href={`/livros/${livro.id}`}>{livro.titulo}</a>
      </h3>
      <p class="livro__autores" title={autores}>
        {autores}
      </p>
      {livro.precoVenda && <p class="livro__preco">{formatarDinheiro(livro.precoVenda)}</p>}
      {item.justificativa && <p class="recomendado__justificativa">{item.justificativa}</p>}
      <BotoesDeAvaliacao
        avaliacao={item.avaliacao}
        sobre={livro.titulo}
        desabilitado={avaliando}
        aoAvaliar={aoAvaliar}
      />
    </article>
  );
}

/**
 * "Recomendados para você" (RNF-REC-001, DEC-03): carrega à parte da grade, então uma falha ou demora da IA não
 * atrapalha a vitrine; sem itens ou com erro, a seção não aparece.
 */
export function Recomendados() {
  const recomendacao = useCarregamento(
    (signal) => cliente.GET('/api/me/recomendacoes', { signal }),
    [],
  );
  const [itens, definirItens] = useState<ItemRecomendado[]>([]);
  const [avaliando, definirAvaliando] = useState<ReadonlySet<number>>(new Set());

  useEffect(() => definirItens(recomendacao.dados ?? []), [recomendacao.dados]);

  async function avaliar(item: ItemRecomendado, avaliacao: AvaliacaoDoItem) {
    definirAvaliando((atuais) => new Set(atuais).add(item.id));
    try {
      const { response } = await cliente.PUT('/api/me/recomendacoes/itens/{id}/avaliacao', {
        params: { path: { id: item.id } },
        body: { avaliacao },
      });
      if (response.ok) {
        definirItens((atuais) => aplicarAvaliacao(atuais, item.id, avaliacao));
      }
    } catch {
      // Sem conexão: a avaliação simplesmente não muda, e o cliente pode tentar de novo.
    } finally {
      definirAvaliando((atuais) => {
        const restantes = new Set(atuais);
        restantes.delete(item.id);
        return restantes;
      });
    }
  }

  if (itens.length === 0) {
    return null;
  }
  return (
    <section class="recomendados" aria-labelledby="recomendados-titulo">
      <h2 id="recomendados-titulo">Recomendados para você</h2>
      <ul class="recomendados__lista">
        {itens.map((item) => (
          <li key={item.id}>
            <CartaoRecomendado
              item={item}
              avaliando={avaliando.has(item.id)}
              aoAvaliar={(avaliacao) => void avaliar(item, avaliacao)}
            />
          </li>
        ))}
      </ul>
    </section>
  );
}
