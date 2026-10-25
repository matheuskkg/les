import './notificacoes.css';

import { lazy } from 'preact-iso';

export { NotificacoesProvider, useNotificacoes, useRecargaPorNotificacao } from './contextoDeNotificacoes';

export const Notificacoes = lazy(() => import('./paginas/Notificacoes'));
