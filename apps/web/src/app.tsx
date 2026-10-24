import { Router, Route, lazy } from 'preact-iso';
import { Cabecalho } from './componentes/Cabecalho';
import { Protegida } from './componentes/Protegida';

const Vitrine = lazy(() => import('./paginas/Vitrine'));
const Login = lazy(() => import('./paginas/Login'));
const Cadastro = lazy(() => import('./paginas/Cadastro'));
const MinhaConta = lazy(() => import('./paginas/MinhaConta'));
const Admin = lazy(() => import('./paginas/Admin'));
const NaoEncontrada = lazy(() => import('./paginas/NaoEncontrada'));

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
          <Route path="/login" component={Login} />
          <Route path="/cadastro" component={Cadastro} />
          <Route path="/minha-conta/*" component={AreaDoCliente} />
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
