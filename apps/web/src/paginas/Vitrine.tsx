export default function Vitrine() {
  return (
    <section class="pagina">
      <div class="destaque">
        <p class="destaque__sobretitulo">Bem-vindo à Livraria</p>
        <h1>Encontre o próximo livro da sua estante</h1>
        <p class="destaque__texto">
          A vitrine de livros será exibida aqui assim que a consulta ao acervo estiver disponível.
          Enquanto isso, você já pode criar sua conta.
        </p>
        <div class="acoes">
          <a class="botao" href="/cadastro">
            Criar conta
          </a>
          <a class="botao botao--secundario" href="/login">
            Entrar
          </a>
        </div>
      </div>

      <h2>Como funciona</h2>
      <div class="cartoes">
        <article class="cartao">
          <h3>Escolha seus livros</h3>
          <p>Consulte o acervo por título, autor ou categoria e monte seu carrinho.</p>
        </article>
        <article class="cartao">
          <h3>Pague com segurança</h3>
          <p>Use um ou mais cartões e aplique cupons de desconto no checkout.</p>
        </article>
        <article class="cartao">
          <h3>Acompanhe o pedido</h3>
          <p>Veja o status da compra e solicite trocas pela sua conta.</p>
        </article>
      </div>
    </section>
  );
}
