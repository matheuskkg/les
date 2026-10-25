import './catalogo.css';

import { lazy } from 'preact-iso';

export const Vitrine = lazy(() => import('./paginas/Vitrine'));
export const Livro = lazy(() => import('./paginas/Livro'));
