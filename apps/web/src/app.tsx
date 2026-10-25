import type { ComponentType } from 'preact';
import { Router, Route } from 'preact-iso';
import { Cabecalho, NaoEncontrada } from './layout';
import { Admin, AdminAnalise, AdminCliente, AdminClientes, AdminPedido, AdminPedidos, AdminTrocas } from './modulos/admin';
import { AvisoFlutuante } from './ui/AvisoFlutuante';
import { Carrinho, GavetaDoCarrinho, useCarrinho } from './modulos/carrinho';
import { Livro, Vitrine } from './modulos/catalogo';
import { Checkout } from './modulos/checkout';
import { Cadastro, MinhaConta } from './modulos/conta';
import { Notificacoes } from './modulos/notificacoes';
import { Login, Protegida } from './modulos/sessao';
import type { Papel } from './modulos/sessao';

/** Rota que só abre com sessão do papel exigido; sem sessão, a Protegida leva ao login. */
function protegida(papel: Papel, Tela: ComponentType) {
  return function TelaProtegida() {
    return (
      <Protegida papel={papel}>
        <Tela />
      </Protegida>
    );
  };
}

const doCliente = (Tela: ComponentType) => protegida('CLIENTE', Tela);
const doAdmin = (Tela: ComponentType) => protegida('ADMIN', Tela);

// Criadas uma vez: um componente novo a cada render remontaria a tela.
const AreaDoCliente = doCliente(MinhaConta);
const CarrinhoDoCliente = doCliente(Carrinho);
const CheckoutDoCliente = doCliente(Checkout);
const NotificacoesDoCliente = doCliente(Notificacoes);
const AreaDoAdmin = doAdmin(Admin);
const NotificacoesDoAdmin = doAdmin(Notificacoes);
const ClientesDoAdmin = doAdmin(AdminClientes);
const ClienteDoAdmin = doAdmin(AdminCliente);
const PedidosDoAdmin = doAdmin(AdminPedidos);
const PedidoDoAdmin = doAdmin(AdminPedido);
const TrocasDoAdmin = doAdmin(AdminTrocas);
const AnaliseDoAdmin = doAdmin(AdminAnalise);

export function App() {
  const { aviso } = useCarrinho();

  return (
    <div class="aplicacao">
      <a class="pular-para-conteudo" href="#conteudo">
        Pular para o conteúdo
      </a>

      <Cabecalho />

      <main class="conteudo" id="conteudo">
        <Router>
          <Route path="/" component={Vitrine} />
          <Route path="/livros/:id" component={Livro} />
          <Route path="/login" component={Login} />
          <Route path="/cadastro" component={Cadastro} />
          <Route path="/carrinho" component={CarrinhoDoCliente} />
          <Route path="/checkout" component={CheckoutDoCliente} />
          <Route path="/minha-conta/notificacoes" component={NotificacoesDoCliente} />
          <Route path="/admin/notificacoes" component={NotificacoesDoAdmin} />
          <Route path="/minha-conta" component={AreaDoCliente} />
          <Route path="/minha-conta/*" component={AreaDoCliente} />
          <Route path="/admin/clientes" component={ClientesDoAdmin} />
          <Route path="/admin/clientes/:id" component={ClienteDoAdmin} />
          <Route path="/admin/pedidos" component={PedidosDoAdmin} />
          <Route path="/admin/pedidos/:id" component={PedidoDoAdmin} />
          <Route path="/admin/trocas" component={TrocasDoAdmin} />
          <Route path="/admin/analise" component={AnaliseDoAdmin} />
          <Route path="/admin" component={AreaDoAdmin} />
          <Route path="/admin/*" component={AreaDoAdmin} />
          <Route default component={NaoEncontrada} />
        </Router>
      </main>

      <GavetaDoCarrinho />
      {aviso && <AvisoFlutuante texto={aviso.texto} link={aviso.link} />}

      <footer class="rodape">
        <p>Livraria — livros para todos os gostos.</p>
      </footer>
    </div>
  );
}
