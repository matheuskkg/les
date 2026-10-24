import assert from 'node:assert/strict';
import test from 'node:test';
import {
  lerNotificacaoDoEvento,
  linkDaReferencia,
  paginaDeNotificacoes,
  prefixoDaArea,
  telaAfetada,
  textoDoContador,
} from './notificacoes.ts';

test('prefixoDaArea separa cliente e administrador', () => {
  assert.equal(prefixoDaArea('CLIENTE'), '/api/me');
  assert.equal(prefixoDaArea('ADMIN'), '/api/admin');
  assert.equal(paginaDeNotificacoes('CLIENTE'), '/minha-conta/notificacoes');
  assert.equal(paginaDeNotificacoes('ADMIN'), '/admin/notificacoes');
});

test('telaAfetada agrupa os tipos pela tela recarregada', () => {
  assert.equal(telaAfetada('CARRINHO_EXPIRADO'), 'carrinho');
  assert.equal(telaAfetada('CHECKOUT_EXPIRANDO'), 'checkout');
  assert.equal(telaAfetada('PEDIDO_AGUARDANDO_DESPACHO'), 'pedido');
  assert.equal(telaAfetada('TROCA_SOLICITADA'), 'troca');
});

test('linkDaReferencia monta o caminho pela área e pelo recurso', () => {
  assert.equal(linkDaReferencia('CLIENTE', 'pedido', 7), '/minha-conta/pedidos/7');
  assert.equal(linkDaReferencia('ADMIN', 'troca', 3), '/admin/trocas/3');
  assert.equal(linkDaReferencia('CLIENTE', 'carrinho', undefined), '/carrinho');
  assert.equal(linkDaReferencia('CLIENTE', 'pedido', undefined), '/minha-conta/pedidos');
  assert.equal(linkDaReferencia('CLIENTE', undefined, undefined), null);
  assert.equal(linkDaReferencia('CLIENTE', 'desconhecido', 1), null);
});

test('textoDoContador esconde zero e limita em 99+', () => {
  assert.equal(textoDoContador(0), '');
  assert.equal(textoDoContador(5), '5');
  assert.equal(textoDoContador(100), '99+');
});

test('lerNotificacaoDoEvento aceita o DTO e recusa corpos inválidos', () => {
  const dto = { id: 10, tipo: 'PEDIDO_APROVADO', mensagem: 'ok', criadaEm: '2026-10-24T12:00:00Z' };
  assert.deepEqual(lerNotificacaoDoEvento(JSON.stringify(dto)), dto);
  assert.equal(lerNotificacaoDoEvento('não é json'), null);
  assert.equal(lerNotificacaoDoEvento(JSON.stringify({ id: 1, tipo: 'OUTRO' })), null);
  assert.equal(lerNotificacaoDoEvento(JSON.stringify({ tipo: 'PEDIDO_APROVADO' })), null);
});
