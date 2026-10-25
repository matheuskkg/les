import assert from 'node:assert/strict';
import test from 'node:test';
import {
  TAMANHO_MAXIMO,
  aplicarAvaliacao,
  assistenteNaRota,
  contarCaracteres,
  enviaAoTeclar,
  lerAberto,
  urlDoConvite,
  mensagemProvisoria,
  podeEnviar,
  type Mensagem,
} from './conversa.ts';

function resposta(id: number): Mensagem {
  return { id, papel: 'ASSISTENTE', texto: 'Veja.', tipo: 'BUSCA', livros: [], enviadaEm: '2026-10-25T12:00:00' };
}

test('RNF-REC-001: o contador conta emoji como um caractere, como o server', () => {
  assert.equal(contarCaracteres('📚ab'), 3);
});

test('RNF-REC-001: envio exige texto, até 500 caracteres e nenhuma resposta pendente', () => {
  assert.equal(podeEnviar('Oi', false), true);
  assert.equal(podeEnviar('   ', false), false);
  assert.equal(podeEnviar('a'.repeat(TAMANHO_MAXIMO), false), true);
  assert.equal(podeEnviar('a'.repeat(TAMANHO_MAXIMO + 1), false), false);
  assert.equal(podeEnviar('Oi', true), false);
});

test('RNF-REC-001: Enter envia e Shift+Enter ou composição não', () => {
  assert.equal(enviaAoTeclar({ key: 'Enter', shiftKey: false, isComposing: false }), true);
  assert.equal(enviaAoTeclar({ key: 'Enter', shiftKey: true, isComposing: false }), false);
  assert.equal(enviaAoTeclar({ key: 'Enter', shiftKey: false, isComposing: true }), false);
  assert.equal(enviaAoTeclar({ key: 'a', shiftKey: false, isComposing: false }), false);
});

test('RNF-REC-001: a mensagem provisória é do cliente, sem espaços nas pontas e com id negativo', () => {
  const mensagem = mensagemProvisoria('  Oi  ', 3, new Date('2026-10-25T12:00:00Z'));
  assert.deepEqual([mensagem.id, mensagem.papel, mensagem.texto], [-3, 'CLIENTE', 'Oi']);
});

test('RNF-REC-001: a última avaliação da resposta vale', () => {
  const mensagens = aplicarAvaliacao(aplicarAvaliacao([resposta(1), resposta(2)], 2, 'NEGATIVA'), 2, 'POSITIVA');
  assert.deepEqual(mensagens.map((mensagem) => mensagem.avaliacao), [undefined, 'POSITIVA']);
});

test('RNF-REC-001: o assistente aparece na loja e não nas telas do administrador', () => {
  assert.equal(assistenteNaRota('/'), true);
  assert.equal(assistenteNaRota('/livros/5'), true);
  assert.equal(assistenteNaRota('/administracao'), true);
  assert.equal(assistenteNaRota('/admin'), false);
  assert.equal(assistenteNaRota('/admin/pedidos'), false);
});

test('RNF-REC-001: o convite do visitante volta à página atual, nunca ao próprio login', () => {
  assert.equal(urlDoConvite('/livros/5', '/livros/5?x=1'), '/login?destino=%2Flivros%2F5%3Fx%3D1');
  assert.equal(urlDoConvite('/login', '/login?destino=%2F'), '/login');
  assert.equal(urlDoConvite('/cadastro', '/cadastro'), '/login');
});

test('RNF-REC-001: o painel abre só com o valor guardado "1"', () => {
  assert.equal(lerAberto('1'), true);
  assert.equal(lerAberto(null), false);
  assert.equal(lerAberto('true'), false);
});
