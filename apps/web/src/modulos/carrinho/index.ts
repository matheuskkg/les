import './carrinho.css';

import { lazy } from 'preact-iso';

export { CarrinhoProvider, useCarrinho, type CarrinhoDto } from './contextoDoCarrinho';
export { CompraEmAndamento } from './componentes/CompraEmAndamento';
export { GavetaDoCarrinho } from './componentes/GavetaDoCarrinho';
export { PrazoDaReserva } from './componentes/PrazoDaReserva';

export const Carrinho = lazy(() => import('./paginas/Carrinho'));
