import assert from 'node:assert/strict';
import test from 'node:test';
import { buscaDaUrl, caminhoInterno, urlDeVolta, urlDoDetalhe } from './url.ts';

test('buscaDaUrl devolve a query string com o ? ou vazia', () => {
  assert.equal(buscaDaUrl('/admin/pedidos'), '');
  assert.equal(buscaDaUrl('/admin/pedidos?status=APROVADA&pagina=2'), '?status=APROVADA&pagina=2');
  assert.equal(buscaDaUrl('/admin/pedidos/123?de=%2Fadmin%2Fpedidos'), '?de=%2Fadmin%2Fpedidos');
});

test('RF-VEN-009: urlDoDetalhe guarda a origem e urlDeVolta a reconstrói', () => {
  const origem = '/admin/pedidos?status=APROVADA&ordem=total,desc&tamanho=50&pagina=3';
  const detalhe = urlDoDetalhe('/admin/pedidos/123', origem);
  const busca = buscaDaUrl(detalhe);

  assert.equal(detalhe, `/admin/pedidos/123?de=${encodeURIComponent(origem)}`);
  assert.equal(urlDeVolta(busca, '/admin/pedidos'), origem);
  assert.equal(new URLSearchParams(busca).get('de'), origem);
});

test('RF-VEN-013: a origem pode ser a consulta de trocas, não só a de pedidos', () => {
  const origem = '/admin/trocas?status=TROCADO&pagina=2';
  const volta = urlDeVolta(buscaDaUrl(urlDoDetalhe('/admin/pedidos/123', origem)), '/admin/pedidos');
  assert.equal(volta, origem);
});

test('RF-CLI-005: a origem pode ser o detalhe do cliente com os filtros dele', () => {
  const origem = '/admin/clientes/42?nome=ana&pagina=2';
  const volta = urlDeVolta(buscaDaUrl(urlDoDetalhe('/admin/pedidos/123', origem)), '/admin/pedidos');
  assert.equal(volta, origem);
});

test('urlDeVolta usa o padrão sem origem e rejeita endereços externos (RF-VEN-009)', () => {
  assert.equal(urlDeVolta('', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?status=APROVADA', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?de=https%3A%2F%2Fexemplo.com', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?de=%2F%2Fexemplo.com', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?de=%2F%5Cexemplo.com', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?de=javascript%3Aalert(1)', '/admin/pedidos'), '/admin/pedidos');

  assert.equal(caminhoInterno('/admin/pedidos?status=APROVADA:sim'), true);
  assert.equal(caminhoInterno('//exemplo.com'), false);
});

test('urlDeVolta rejeita caracteres de controle que o parser de URL remove (redirect externo)', () => {
  // O navegador transforma "/\t/evil.example" em "//evil.example" antes de resolver o href.
  assert.equal(urlDeVolta('?de=%2F%09%2Fevil.example', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?de=%2F%0D%2Fevil.example', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?de=%2F%0A%2Fevil.example', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?de=%2F%00%2Fevil.example', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?de=%2F%1F%2Fevil.example', '/admin/pedidos'), '/admin/pedidos');
  assert.equal(urlDeVolta('?de=%2F%7F%2Fevil.example', '/admin/pedidos'), '/admin/pedidos');
  // O mesmo ataque no "Voltar para a vitrine" cai na raiz, não em outro site.
  assert.equal(urlDeVolta('?de=%2F%09%2Fevil.example', '/'), '/');

  assert.equal(caminhoInterno('/\t/evil.example'), false);
  assert.equal(caminhoInterno('/admin/pedidos?nome=ana'), true);
});
