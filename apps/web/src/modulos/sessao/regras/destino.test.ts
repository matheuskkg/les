import assert from 'node:assert/strict';
import test from 'node:test';
import { destinoSeguro } from './destino.ts';
import type { Usuario } from '../api';

const origem = 'http://localhost:5173';
const cliente: Usuario = { usuarioId: 1, email: 'cliente@exemplo.com', papel: 'CLIENTE', nome: 'Cliente' };
const admin: Usuario = { usuarioId: 2, email: 'admin@exemplo.com', papel: 'ADMIN', nome: 'Administração' };

test('destinoSeguro aceita caminho interno da mesma origem', () => {
  assert.equal(
    destinoSeguro('/minha-conta/pedidos?pagina=2', cliente, origem),
    '/minha-conta/pedidos?pagina=2',
  );
  assert.equal(destinoSeguro('/admin/clientes', admin, origem), '/admin/clientes');
});

test('destinoSeguro usa a área padrão sem destino', () => {
  assert.equal(destinoSeguro(undefined, cliente, origem), '/minha-conta');
  assert.equal(destinoSeguro('', cliente, origem), '/minha-conta');
  assert.equal(destinoSeguro(undefined, admin, origem), '/admin/clientes');
});

test('destinoSeguro recusa outra origem, esquemas e barras invertidas', () => {
  assert.equal(destinoSeguro('https://evil.example/admin', admin, origem), '/admin/clientes');
  assert.equal(destinoSeguro('//evil.example/admin', admin, origem), '/admin/clientes');
  assert.equal(destinoSeguro('/\\evil.example/admin', admin, origem), '/admin/clientes');
  assert.equal(destinoSeguro('javascript:alert(1)', cliente, origem), '/minha-conta');
  assert.equal(destinoSeguro('admin', admin, origem), '/admin/clientes');
});

test('destinoSeguro recusa caracteres de controle que o parser de URL remove', () => {
  assert.equal(destinoSeguro('/\t/evil.example/admin', admin, origem), '/admin/clientes');
  assert.equal(destinoSeguro('/\n/evil.example/minha-conta', cliente, origem), '/minha-conta');
});

test('destinoSeguro respeita o papel do usuário', () => {
  assert.equal(destinoSeguro('/admin', cliente, origem), '/minha-conta');
  assert.equal(destinoSeguro('/minha-conta', admin, origem), '/admin/clientes');
  assert.equal(destinoSeguro('/administracao', cliente, origem), '/administracao');
});
