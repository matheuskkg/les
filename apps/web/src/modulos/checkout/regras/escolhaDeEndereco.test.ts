import assert from 'node:assert/strict';
import test from 'node:test';
import { enderecoVazio } from '../../conta/regras/cadastroDeCliente.ts';
import { ehCadastrado, escolhaInicial, escolhaParaApi, resumoDoEndereco } from './escolhaDeEndereco.ts';

test('RF-VEN-005: endereço cadastrado vai só pelo id', () => {
  assert.deepEqual(escolhaParaApi({ enderecoId: 2 }), { enderecoId: 2 });
});

test('RF-VEN-005: endereço novo vai inteiro, com o CEP só em dígitos', () => {
  const novo = { ...enderecoVazio(), nomeCurto: ' Presente ', logradouro: 'Rua A', cep: '69005-040', uf: 'AM' };
  const api = escolhaParaApi({ novo });
  assert.ok('novo' in api && api.novo);
  assert.equal(api.novo.cep, '69005040');
  assert.equal(api.novo.nomeCurto, 'Presente');
  assert.equal(api.enderecoId, undefined);
});

test('RF-VEN-005: a escolha inicial é o primeiro endereço cadastrado', () => {
  const enderecos = [{ id: 4 }, { id: 9 }] as Parameters<typeof escolhaInicial>[0];
  assert.deepEqual(escolhaInicial(enderecos), { enderecoId: 4 });
  assert.equal(escolhaInicial([]), null);
  assert.ok(ehCadastrado({ enderecoId: 4 }, 4));
  assert.ok(!ehCadastrado({ novo: enderecoVazio() }, 4));
  assert.ok(!ehCadastrado(null, 4));
});

test('resumoDoEndereco mostra o endereço em uma linha com o CEP formatado', () => {
  assert.equal(
    resumoDoEndereco({ logradouro: 'Rua A', numero: '10', bairro: 'Centro', cidade: 'Manaus', uf: 'AM', cep: '69005040' }),
    'Rua A, 10 — Centro, Manaus/AM, CEP 69005-040',
  );
});
