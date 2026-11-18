import assert from 'node:assert/strict';
import test from 'node:test';
import { itemAtivoDaNavbar } from './itemAtivo.ts';

const NOTIFICACOES_DO_CLIENTE = '/minha-conta/notificacoes';
const NOTIFICACOES_DO_ADMIN = '/admin/notificacoes';

test('vitrine cobre a raiz, a busca e o detalhe do livro (ALT-08)', () => {
  assert.equal(itemAtivoDaNavbar('/', NOTIFICACOES_DO_CLIENTE), 'vitrine');
  assert.equal(itemAtivoDaNavbar('/livros', NOTIFICACOES_DO_CLIENTE), 'vitrine');
  assert.equal(itemAtivoDaNavbar('/livros/1', NOTIFICACOES_DO_CLIENTE), 'vitrine');
  assert.equal(itemAtivoDaNavbar('/livros/42', NOTIFICACOES_DO_CLIENTE), 'vitrine');
});

test('carrinho cobre o carrinho e o checkout (ALT-08)', () => {
  assert.equal(itemAtivoDaNavbar('/carrinho', NOTIFICACOES_DO_CLIENTE), 'carrinho');
  assert.equal(itemAtivoDaNavbar('/checkout', NOTIFICACOES_DO_CLIENTE), 'carrinho');
});

test('minha conta cobre o resumo e as sub-rotas, menos notificações (ALT-08)', () => {
  assert.equal(itemAtivoDaNavbar('/minha-conta', NOTIFICACOES_DO_CLIENTE), 'minha-conta');
  assert.equal(itemAtivoDaNavbar('/minha-conta/pedidos', NOTIFICACOES_DO_CLIENTE), 'minha-conta');
  assert.equal(itemAtivoDaNavbar('/minha-conta/pedidos/7', NOTIFICACOES_DO_CLIENTE), 'minha-conta');
  assert.equal(itemAtivoDaNavbar('/minha-conta/enderecos', NOTIFICACOES_DO_CLIENTE), 'minha-conta');
  assert.equal(itemAtivoDaNavbar('/minha-conta/notificacoes', NOTIFICACOES_DO_CLIENTE), 'notificacoes');
});

test('administração cobre todas as seções, menos notificações (ALT-08)', () => {
  assert.equal(itemAtivoDaNavbar('/admin/clientes', NOTIFICACOES_DO_ADMIN), 'administracao');
  assert.equal(itemAtivoDaNavbar('/admin/clientes/12', NOTIFICACOES_DO_ADMIN), 'administracao');
  assert.equal(itemAtivoDaNavbar('/admin/pedidos/34', NOTIFICACOES_DO_ADMIN), 'administracao');
  assert.equal(itemAtivoDaNavbar('/admin/trocas', NOTIFICACOES_DO_ADMIN), 'administracao');
  assert.equal(itemAtivoDaNavbar('/admin/analise', NOTIFICACOES_DO_ADMIN), 'administracao');
  assert.equal(itemAtivoDaNavbar('/admin/assistente', NOTIFICACOES_DO_ADMIN), 'administracao');
  assert.equal(itemAtivoDaNavbar('/admin/notificacoes', NOTIFICACOES_DO_ADMIN), 'notificacoes');
});

test('rotas fora da navegação não destacam nenhum item (ALT-08)', () => {
  assert.equal(itemAtivoDaNavbar('/login', NOTIFICACOES_DO_CLIENTE), null);
  assert.equal(itemAtivoDaNavbar('/cadastro', NOTIFICACOES_DO_CLIENTE), null);
  assert.equal(itemAtivoDaNavbar('/rota-inexistente', NOTIFICACOES_DO_CLIENTE), null);
});

test('sem sessão não há item de notificações para destacar (ALT-08)', () => {
  assert.equal(itemAtivoDaNavbar('/minha-conta/notificacoes', ''), 'minha-conta');
  assert.equal(itemAtivoDaNavbar('/', ''), 'vitrine');
});
