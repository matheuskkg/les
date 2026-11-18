import assert from 'node:assert/strict';
import test from 'node:test';
import { rotuloDoCarrinho } from './rotuloDoCarrinho.ts';

test('botão só com ícone mantém o nome Carrinho quando o carrinho está vazio (ALT-12)', () => {
  assert.equal(rotuloDoCarrinho(0), 'Carrinho');
});

test('nome acessível informa a quantidade com o plural correto (ALT-12)', () => {
  assert.equal(rotuloDoCarrinho(1), 'Carrinho: 1 item');
  assert.equal(rotuloDoCarrinho(2), 'Carrinho: 2 itens');
  assert.equal(rotuloDoCarrinho(10), 'Carrinho: 10 itens');
});

test('quantidade inválida é tratada como carrinho vazio (ALT-12)', () => {
  assert.equal(rotuloDoCarrinho(-1), 'Carrinho');
});
