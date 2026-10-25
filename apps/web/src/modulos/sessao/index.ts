import { lazy } from 'preact-iso';

export { SessaoProvider, useSessao } from './contextoDeSessao';
export { Protegida } from './componentes/Protegida';
export { entrar, obterSessao, type Papel, type Usuario } from './api';

export const Login = lazy(() => import('./paginas/Login'));
