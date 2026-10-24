import { Router, Route } from 'preact-iso';
import { Cabecalho } from './componentes/Cabecalho';
import { Protegida } from './componentes/Protegida';
import {
  Admin,
  AdminCliente,
  AdminClientes,
  Cadastro,
  Livro,
  Login,
  MinhaConta,
  NaoEncontrada,
  Notificacoes,
  Vitrine,
} from './paginas/telas';

function AreaDoCliente() {
  return (
    <Protegida papel="CLIENTE">
      <MinhaConta />
    </Protegida>
  );
}

function AreaDoAdmin() {
  return (
    <Protegida papel="ADMIN">
      <Admin />
    </Protegida>
  );
}

function NotificacoesDoCliente() {
  return (
    <Protegida papel="CLIENTE">
      <Notificacoes />
    </Protegida>
  );
}

function NotificacoesDoAdmin() {
  return (
    <Protegida papel="ADMIN">
      <Notificacoes />
    </Protegida>
  );
}

function ClientesDoAdmin() {
  return (
    <Protegida papel="ADMIN">
      <AdminClientes />
    </Protegida>
  );
}

function ClienteDoAdmin() {
  return (
    <Protegida papel="ADMIN">
      <AdminCliente />
    </Protegida>
  );
}

export function App() {
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
          <Route path="/minha-conta/notificacoes" component={NotificacoesDoCliente} />
          <Route path="/admin/notificacoes" component={NotificacoesDoAdmin} />
          <Route path="/minha-conta" component={AreaDoCliente} />
          <Route path="/minha-conta/*" component={AreaDoCliente} />
          <Route path="/admin/clientes" component={ClientesDoAdmin} />
          <Route path="/admin/clientes/:id" component={ClienteDoAdmin} />
          <Route path="/admin" component={AreaDoAdmin} />
          <Route path="/admin/*" component={AreaDoAdmin} />
          <Route default component={NaoEncontrada} />
        </Router>
      </main>

      <footer class="rodape">
        <p>Livraria — livros para todos os gostos.</p>
      </footer>
    </div>
  );
}
