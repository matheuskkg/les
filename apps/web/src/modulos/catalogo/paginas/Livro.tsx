import { useRoute } from 'preact-iso';
import { cliente } from '../../../api/cliente';
import { formatarDecimal, formatarDinheiro } from '../../../compartilhado/formatacao';
import { useCarregamento } from '../../../compartilhado/useCarregamento';
import { BotaoLink } from '../../../ui/Botao';
import { CapaDeLivro } from '../../../ui/CapaDeLivro';
import { Erro } from '../../../ui/Erro';
import { Esqueleto } from '../../../ui/Esqueleto';
import { AdicionarAoCarrinho } from '../componentes/AdicionarAoCarrinho';

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
      <p class="pagina__introducao">O livro consultado não existe, foi inativado ou saiu da vitrine.</p>
      <div class="acoes">
        <BotaoLink href="/">Voltar para a vitrine</BotaoLink>
      </div>
    </section>
  );
}

/** Detalhe do livro (RF-LIV-005): capa, preço e compra lado a lado, com a ficha técnica e a sinopse abaixo. */
export default function Livro() {
  const { params } = useRoute();
  const codigo = codigoDaRota(params.id);
  const { dados: livro, problema, carregando } = useCarregamento(
    (signal) => cliente.GET('/api/livros/{id}', { params: { path: { id: codigo ?? 0 } }, signal }),
    [codigo],
  );

  if (codigo === null || problema?.status === 404) {
    return <NaoEncontrado />;
  }

  if (carregando && livro?.id !== codigo) {
    return (
      <section class="pagina livro-detalhe">
        <Esqueleto linhas={6} rotulo="Carregando livro…" />
      </section>
    );
  }

  if (livro === null) {
    return (
      <section class="pagina pagina--estreita">
        <h1>Livro</h1>
        <Erro problema={problema} />
        <div class="acoes">
          <BotaoLink href="/">Voltar para a vitrine</BotaoLink>
        </div>
      </section>
    );
  }

  const linhas = [
    { rotulo: 'Editora', valor: livro.editora },
    { rotulo: 'Edição', valor: livro.edicao },
    { rotulo: 'Ano', valor: String(livro.ano) },
    { rotulo: 'Páginas', valor: String(livro.paginas) },
    { rotulo: 'ISBN', valor: livro.isbn },
    { rotulo: 'Código de barras', valor: livro.codigoBarras },
    { rotulo: 'Código', valor: String(livro.id) },
    {
      rotulo: 'Dimensões',
      valor: `${formatarDecimal(livro.altura)} × ${formatarDecimal(livro.largura)} × ${formatarDecimal(
        livro.profundidade,
      )} cm`,
    },
    { rotulo: 'Peso', valor: `${formatarDecimal(livro.peso)} kg` },
  ];
  const disponivel = livro.disponivel > 0;

  return (
    <article class="pagina livro-detalhe">
      <a class="livro-detalhe__voltar" href="/">
        ← Voltar para a vitrine
      </a>

      <div class="livro-detalhe__principal">
        <CapaDeLivro
          titulo={livro.titulo}
          autores={livro.autores}
          categoria={livro.categorias[0]?.nome}
          tamanho="grande"
        />

        <div class="livro-detalhe__compra">
          <header>
            <h1>{livro.titulo}</h1>
            <p class="livro-detalhe__autores">{livro.autores.join('; ')}</p>
            {livro.categorias.length > 0 && (
              <ul class="livro__categorias" aria-label="Categorias">
                {livro.categorias.map((categoria) => (
                  <li key={categoria.id}>
                    <a href={`/?categoria=${categoria.id}`}>{categoria.nome}</a>
                  </li>
                ))}
              </ul>
            )}
          </header>

          <div class="cartao livro-detalhe__resumo">
            <p class="livro__preco">{livro.precoVenda ? formatarDinheiro(livro.precoVenda) : 'Indisponível'}</p>
            <p class={`livro__estoque${disponivel ? '' : ' livro__estoque--esgotado'}`}>
              {disponivel ? `Em estoque: ${livro.disponivel}` : 'Esgotado'}
            </p>
            {livro.precoVenda && disponivel && (
              <AdicionarAoCarrinho livroId={livro.id} disponivel={livro.disponivel} />
            )}
          </div>

          <section>
            <h2>Sinopse</h2>
            <p>{livro.sinopse.trim() !== '' ? livro.sinopse : 'Sinopse não informada.'}</p>
          </section>
        </div>
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
    </article>
  );
}
