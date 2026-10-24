import { useEffect, useState } from 'preact/hooks';
import { useRoute } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../api/cliente';
import type { components } from '../api/esquema';
import { Erro } from '../componentes/Erro';
import { formatarDecimal, formatarDinheiro } from '../formatacao';

type LivroDetalhe = components['schemas']['LivroDetalheDto'];

/** Resultado da consulta, preso ao código pedido para não exibir livro de outra URL. */
interface Resultado {
  codigo: number;
  livro: LivroDetalhe | null;
  problema: ProblemaDetalhe | null;
  falha: string | null;
}

function codigoDaRota(valor: string | undefined): number | null {
  if (valor === undefined || !/^\d+$/.test(valor)) {
    return null;
  }
  const codigo = Number(valor);
  return Number.isSafeInteger(codigo) ? codigo : null;
}

function NaoEncontrado() {
  return (
    <section class="pagina pagina--estreita">
      <h1>Livro não encontrado.</h1>
      <p class="pagina__introducao">
        O livro consultado não existe, foi inativado ou saiu da vitrine.
      </p>
      <div class="acoes">
        <a class="botao" href="/">
          Voltar para a vitrine
        </a>
      </div>
    </section>
  );
}

export default function Livro() {
  const { params } = useRoute();
  const codigo = codigoDaRota(params.id);

  const [resultado, definirResultado] = useState<Resultado | null>(null);

  useEffect(() => {
    if (codigo === null) {
      return;
    }

    const controle = new AbortController();
    const id = codigo;

    async function carregar(): Promise<void> {
      try {
        const { data, error, response } = await cliente.GET('/api/livros/{id}', {
          params: { path: { id } },
          signal: controle.signal,
        });
        if (controle.signal.aborted) {
          return;
        }
        if (!response.ok) {
          definirResultado({
            codigo: id,
            livro: null,
            problema: interpretarProblema(error, response.status),
            falha: null,
          });
          return;
        }
        definirResultado({ codigo: id, livro: data ?? null, problema: null, falha: null });
      } catch {
        if (!controle.signal.aborted) {
          definirResultado({
            codigo: id,
            livro: null,
            problema: null,
            falha: 'Não foi possível carregar o livro. Tente novamente.',
          });
        }
      }
    }

    void carregar();
    return () => controle.abort();
  }, [codigo]);

  if (codigo === null) {
    return <NaoEncontrado />;
  }

  if (resultado === null || resultado.codigo !== codigo) {
    return (
      <section class="pagina">
        <p class="aviso" role="status">
          Carregando livro…
        </p>
      </section>
    );
  }

  if (resultado.problema?.status === 404) {
    return <NaoEncontrado />;
  }

  const livro = resultado.livro;
  if (resultado.problema !== null || resultado.falha !== null || livro === null) {
    return (
      <section class="pagina pagina--estreita">
        <h1>Livro</h1>
        <Erro problema={resultado.problema} mensagem={resultado.falha} />
        <div class="acoes">
          <a class="botao" href="/">
            Voltar para a vitrine
          </a>
        </div>
      </section>
    );
  }

  const linhas = [
    { rotulo: 'Código', valor: String(livro.id) },
    { rotulo: 'Autores', valor: livro.autores.join('; ') },
    { rotulo: 'Editora', valor: livro.editora },
    { rotulo: 'Edição', valor: livro.edicao },
    { rotulo: 'Ano', valor: String(livro.ano) },
    { rotulo: 'ISBN', valor: livro.isbn },
    { rotulo: 'Código de barras', valor: livro.codigoBarras },
    { rotulo: 'Páginas', valor: String(livro.paginas) },
    {
      rotulo: 'Categorias',
      valor: livro.categorias.map((categoria) => categoria.nome).join('; ') || '—',
    },
    {
      rotulo: 'Dimensões',
      valor: `${formatarDecimal(livro.altura)} × ${formatarDecimal(
        livro.largura,
      )} × ${formatarDecimal(livro.profundidade)} cm`,
    },
    { rotulo: 'Peso', valor: `${formatarDecimal(livro.peso)} kg` },
  ];

  return (
    <article class="pagina livro-detalhe">
      <a class="livro-detalhe__voltar" href="/">
        ← Voltar para a vitrine
      </a>

      <header class="livro-detalhe__cabecalho">
        <h1>{livro.titulo}</h1>
        <p class="livro-detalhe__autores">{livro.autores.join('; ')}</p>
      </header>

      <div class="cartao livro-detalhe__resumo">
        <p class="livro__preco">
          {livro.precoVenda ? formatarDinheiro(livro.precoVenda) : 'Indisponível'}
        </p>
        <p class="livro__estoque">
          {livro.disponivel > 0 ? `Em estoque: ${livro.disponivel}` : 'Esgotado'}
        </p>
      </div>

      <section class="cartao">
        <h2>Ficha do livro</h2>
        <dl class="detalhe__campos">
          {linhas.map((linha) => (
            <div key={linha.rotulo}>
              <dt>{linha.rotulo}</dt>
              <dd>{linha.valor}</dd>
            </div>
          ))}
        </dl>
      </section>

      <section class="cartao">
        <h2>Sinopse</h2>
        <p>{livro.sinopse.trim() !== '' ? livro.sinopse : 'Sinopse não informada.'}</p>
      </section>
    </article>
  );
}
