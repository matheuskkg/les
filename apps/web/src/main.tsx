import './estilos/tokens.css';
import './estilos/base.css';
import './estilos/ui.css';
import { render } from 'preact';
import { LocationProvider } from 'preact-iso';
import { App } from './app';
import { CarrinhoProvider } from './modulos/carrinho';
import { Vitrine } from './modulos/catalogo';
import { NotificacoesProvider } from './modulos/notificacoes';
import { Login, SessaoProvider } from './modulos/sessao';

const elemento = document.getElementById('app');
if (!elemento) {
  throw new Error('Elemento #app não encontrado.');
}

render(
  <LocationProvider>
    <SessaoProvider preCarregar={{ login: Login.preload, vitrine: Vitrine.preload }}>
      <NotificacoesProvider>
        <CarrinhoProvider>
          <App />
        </CarrinhoProvider>
      </NotificacoesProvider>
    </SessaoProvider>
  </LocationProvider>,
  elemento,
);
