import assert from 'node:assert/strict';
import test from 'node:test';
import {
  apagarDigitoAntesDoSeparador,
  aplicarMascara,
  posicaoDoCaret,
  type Mascara,
} from './mascaras.ts';

/** Simula a digitação normal: aplica a máscara a cada tecla, informando o valor anterior. */
function digitar(mascara: Mascara, digitos: string): string {
  let valor = '';
  for (const digito of digitos) {
    valor = aplicarMascara(mascara, valor + digito, valor);
  }
  return valor;
}

test('cpf (RF-CLI-001) monta a máscara conforme os dígitos chegam', () => {
  assert.equal(aplicarMascara('cpf', ''), '');
  assert.equal(aplicarMascara('cpf', '529'), '529');
  assert.equal(aplicarMascara('cpf', '5299'), '529.9');
  assert.equal(aplicarMascara('cpf', '529982'), '529.982');
  assert.equal(aplicarMascara('cpf', '529982247'), '529.982.247');
  assert.equal(aplicarMascara('cpf', '52998224725'), '529.982.247-25');
  assert.equal(aplicarMascara('cpf', '529.982.247-25'), '529.982.247-25');
  assert.equal(aplicarMascara('cpf', '529982247259999'), '529.982.247-25');
});

test('cep (RF-CLI-001) monta a máscara de 8 dígitos', () => {
  assert.equal(aplicarMascara('cep', ''), '');
  assert.equal(aplicarMascara('cep', '01001'), '01001');
  assert.equal(aplicarMascara('cep', '01001000'), '01001-000');
  assert.equal(aplicarMascara('cep', '01001-000'), '01001-000');
  assert.equal(aplicarMascara('cep', '0100100099'), '01001-000');
});

test('ddd (RF-CLI-001) aceita só dois dígitos', () => {
  assert.equal(aplicarMascara('ddd', ''), '');
  assert.equal(aplicarMascara('ddd', '1'), '1');
  assert.equal(aplicarMascara('ddd', '11'), '11');
  assert.equal(aplicarMascara('ddd', '1a1'), '11');
  assert.equal(aplicarMascara('ddd', '119'), '11');
});

test('telefone (RF-CLI-001) usa 0000-0000 ou 00000-0000 e limita a 9 dígitos na digitação', () => {
  assert.equal(aplicarMascara('telefone', ''), '');
  assert.equal(digitar('telefone', '9765'), '9765');
  assert.equal(digitar('telefone', '97654'), '9765-4');
  assert.equal(digitar('telefone', '87654321'), '8765-4321');
  assert.equal(digitar('telefone', '976543210'), '97654-3210');
  /* Um dígito a mais na digitação normal continua sendo ignorado, sem virar colagem. */
  assert.equal(digitar('telefone', '9765432109'), '97654-3210');
  assert.equal(aplicarMascara('telefone', '9765432109999'), '97654-3210');
});

test('telefone (RF-CLI-001) em colagem/autopreenchimento descarta o DDD', () => {
  assert.equal(aplicarMascara('telefone', '(11) 97654-3210', ''), '97654-3210');
  assert.equal(aplicarMascara('telefone', '(11) 8765-4321', ''), '8765-4321');
  assert.equal(aplicarMascara('telefone', '11976543210', ''), '97654-3210');
  assert.equal(aplicarMascara('telefone', '1187654321', ''), '8765-4321');
  assert.equal(aplicarMascara('telefone', '9765432109999', ''), '43210-9999');
});

test('numeroDoCartao (DEC-10, RN-CLI-004) agrupa de 4 em 4 com espaço, até 19 dígitos', () => {
  assert.equal(aplicarMascara('numeroDoCartao', ''), '');
  assert.equal(aplicarMascara('numeroDoCartao', '4111'), '4111');
  assert.equal(aplicarMascara('numeroDoCartao', '41111'), '4111 1');
  assert.equal(aplicarMascara('numeroDoCartao', '4111111111111111'), '4111 1111 1111 1111');
  assert.equal(aplicarMascara('numeroDoCartao', '5555 5555 5555 4444'), '5555 5555 5555 4444');
  assert.equal(
    aplicarMascara('numeroDoCartao', '41111111111111111111234'),
    '4111 1111 1111 1111 111',
  );
});

test('numeroDoCartao (DEC-18) agrupa American Express em 4-6-5', () => {
  assert.equal(aplicarMascara('numeroDoCartao', '37'), '37');
  assert.equal(aplicarMascara('numeroDoCartao', '378282'), '3782 82');
  assert.equal(aplicarMascara('numeroDoCartao', '3782822463'), '3782 822463');
  assert.equal(aplicarMascara('numeroDoCartao', '378282246310005'), '3782 822463 10005');
  assert.equal(aplicarMascara('numeroDoCartao', '341234567890123'), '3412 345678 90123');
  assert.equal(aplicarMascara('numeroDoCartao', '3782822463100051234'), '3782 822463 10005');
  assert.equal(aplicarMascara('numeroDoCartao', '4477123456789012'), '4477 1234 5678 9012');
});

test('cvv (RN-CLI-004) aceita só dígitos, até 4', () => {
  assert.equal(aplicarMascara('cvv', ''), '');
  assert.equal(aplicarMascara('cvv', '12'), '12');
  assert.equal(aplicarMascara('cvv', '1a2'), '12');
  assert.equal(aplicarMascara('cvv', '123456'), '1234');
});

test('posicaoDoCaret mantém o cursor após a mesma quantidade de dígitos (RF-CLI-001)', () => {
  assert.equal(posicaoDoCaret('', 0, ''), 0);
  assert.equal(posicaoDoCaret('529.9', 0, '529.9'), 0);
  assert.equal(posicaoDoCaret('5299', 2, '529.9'), 2);
  assert.equal(posicaoDoCaret('529982', 4, '529.982'), 5);
  assert.equal(posicaoDoCaret('5299', 4, '529.9'), 5);
  assert.equal(posicaoDoCaret('97654', 5, '9765-4'), 6);
  assert.equal(posicaoDoCaret('52998224725', 11, '529.982.247-25'), 14);
});

test('backspace após separador apaga o dígito anterior, sem prender o cursor (RF-CLI-001)', () => {
  const cpf = apagarDigitoAntesDoSeparador('529.982.247-25', '529.982.24725', 11);
  assert.ok(cpf !== null);
  assert.deepEqual(cpf, { valor: '529.982.24-25', caret: 10 });
  assert.equal(aplicarMascara('cpf', cpf.valor), '529.982.242-5');
  assert.equal(posicaoDoCaret(cpf.valor, cpf.caret, '529.982.242-5'), 10);

  const telefone = apagarDigitoAntesDoSeparador('9765-4321', '97654321', 4);
  assert.ok(telefone !== null);
  assert.deepEqual(telefone, { valor: '976-4321', caret: 3 });
  assert.equal(aplicarMascara('telefone', telefone.valor), '9764-321');
  assert.equal(posicaoDoCaret(telefone.valor, telefone.caret, '9764-321'), 3);

  /* Backspace normal (sem separador) e remoção de seleção maior não são ajustados. */
  assert.equal(apagarDigitoAntesDoSeparador('9765-4321', '9765-432', 8), null);
  assert.equal(apagarDigitoAntesDoSeparador('529.982.247-25', '529.982.247', 11), null);
  assert.equal(apagarDigitoAntesDoSeparador('529.982.247-25', '529.982.24-25', 10), null);
});
