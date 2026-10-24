import { render } from 'preact';
import { LocationProvider } from 'preact-iso';
import { App } from './app';
import { NotificacoesProvider } from './contextoDeNotificacoes';
import { SessaoProvider } from './contextoDeSessao';
import './estilos.css';

const elemento = document.getElementById('app');
if (!elemento) {
  throw new Error('Elemento #app não encontrado.');
}

render(
  <LocationProvider>
    <SessaoProvider>
      <NotificacoesProvider>
        <App />
      </NotificacoesProvider>
    </SessaoProvider>
  </LocationProvider>,
  elemento,
);
