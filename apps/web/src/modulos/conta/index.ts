import './conta.css';

import { lazy } from 'preact-iso';

export { CamposDoCartao } from './componentes/CamposDoCartao';
export { CamposDoEndereco } from './componentes/CamposDoEndereco';

export const Cadastro = lazy(() => import('./paginas/Cadastro'));
export const MinhaConta = lazy(() => import('./paginas/MinhaConta'));
