import './admin.css';

import { lazy } from 'preact-iso';

export const Admin = lazy(() => import('./paginas/Admin'));
export const AdminAnalise = lazy(() => import('./paginas/AdminAnalise'));
export const AdminCliente = lazy(() => import('./paginas/AdminCliente'));
export const AdminClientes = lazy(() => import('./paginas/AdminClientes'));
export const AdminPedido = lazy(() => import('./paginas/AdminPedido'));
export const AdminPedidos = lazy(() => import('./paginas/AdminPedidos'));
export const AdminTrocas = lazy(() => import('./paginas/AdminTrocas'));
export { NavegacaoDoAdmin } from './componentes/NavegacaoDoAdmin';
