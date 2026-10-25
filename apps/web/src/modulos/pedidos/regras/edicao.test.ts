import assert from 'node:assert/strict';
import test from 'node:test';
import { textoDaEdicao } from './edicao.ts';

test('a edição não repete a palavra quando o dado já a contém (RF-CLI-005)', () => {
  assert.equal(textoDaEdicao('1ª edição'), '1ª edição');
  assert.equal(textoDaEdicao('2ª Edição'), '2ª Edição');
  assert.equal(textoDaEdicao('3ª edicao'), '3ª edicao');
  assert.equal(textoDaEdicao('4ª ed.'), '4ª ed.');
  assert.equal(textoDaEdicao('  5ª edição  '), '5ª edição');
});

test('a edição reconhece "ed", "ed." e "edição" em qualquer posição', () => {
  assert.equal(textoDaEdicao('2ª ed'), '2ª ed');
  assert.equal(textoDaEdicao('2ª ED.'), '2ª ED.');
  assert.equal(textoDaEdicao('Edição 3'), 'Edição 3');
  assert.equal(textoDaEdicao('1ª edição revisada'), '1ª edição revisada');
  assert.equal(textoDaEdicao('Edicao 4, revista'), 'Edicao 4, revista');
});

test('a edição sem a palavra recebe o complemento uma única vez', () => {
  assert.equal(textoDaEdicao('3ª'), '3ª edição');
  assert.equal(textoDaEdicao(''), '');
});

test('a edição não confunde "ed" com o começo de outra palavra', () => {
  assert.equal(textoDaEdicao('Editora Abril'), 'Editora Abril edição');
  assert.equal(textoDaEdicao('Reduzida'), 'Reduzida edição');
});
