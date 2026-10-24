import { lazy } from 'preact-iso';

/**
 * Telas carregadas sob demanda. Ficam reunidas aqui para que o Router e o contexto
 * de sessão usem a mesma instância (e possam pré-carregar antes de navegar).
 */
export const Vitrine = lazy(() => import('./Vitrine'));
export const Livro = lazy(() => import('./Livro'));
export const Login = lazy(() => import('./Login'));
export const Cadastro = lazy(() => import('./Cadastro'));
export const MinhaConta = lazy(() => import('./MinhaConta'));
export const Admin = lazy(() => import('./Admin'));
export const Notificacoes = lazy(() => import('./Notificacoes'));
export const AdminClientes = lazy(() => import('./AdminClientes'));
export const AdminCliente = lazy(() => import('./AdminCliente'));
export const AdminPedidos = lazy(() => import('./AdminPedidos'));
export const AdminPedido = lazy(() => import('./AdminPedido'));
export const AdminTrocas = lazy(() => import('./AdminTrocas'));
export const Carrinho = lazy(() => import('./Carrinho'));
export const Checkout = lazy(() => import('./Checkout'));
export const NaoEncontrada = lazy(() => import('./NaoEncontrada'));
