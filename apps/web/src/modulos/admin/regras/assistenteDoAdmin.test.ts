import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  cadeiaAlterada,
  corpoDaCadeia,
  formatarChamadasHttp,
  formatarLatencia,
  formatarTaxa,
  lerPeriodoDasMetricas,
  moverProvedor,
  rotuloDoDia,
  rotuloDoResultado,
  somarDias,
  urlDoAssistente,
} from './assistenteDoAdmin.ts';

const HOJE = new Date(2026, 9, 25, 12, 0);

function provedor(id: number, ativo = true) {
  return { id, nome: `p${id}`, ordem: id, ativo, chaveConfigurada: true };
}

test('RNF-REC-001: sem período na URL, as métricas mostram os 30 últimos dias contando hoje', () => {
  assert.deepEqual(lerPeriodoDasMetricas('', HOJE), { inicio: '2026-09-26', fim: '2026-10-25' });
});

test('RNF-REC-001: lê o período da URL e ignora dias malformados', () => {
  assert.deepEqual(lerPeriodoDasMetricas('?inicio=2026-10-01&fim=2026-10-10', HOJE), {
    inicio: '2026-10-01',
    fim: '2026-10-10',
  });
  assert.deepEqual(lerPeriodoDasMetricas('?inicio=2026-13-01&fim=2026-10-10', HOJE).inicio, '2026-09-11');
  assert.deepEqual(lerPeriodoDasMetricas('?inicio=2026-10-10&fim=2026-10-01', HOJE), {
    inicio: '2026-10-10',
    fim: '2026-10-01',
  });
  assert.equal(urlDoAssistente({ inicio: '2026-10-01', fim: '2026-10-10' }),
    '/admin/assistente?inicio=2026-10-01&fim=2026-10-10');
});

test('soma dias atravessando mês e ano', () => {
  assert.equal(somarDias('2026-03-01', -1), '2026-02-28');
  assert.equal(somarDias('2025-12-31', 1), '2026-01-01');
});

test('RNF-REC-001: subir e descer trocam o provedor com o vizinho, sem sair da lista nas pontas', () => {
  const lista = [provedor(1), provedor(2), provedor(3)];
  assert.deepEqual(moverProvedor(lista, 2, -1).map((p) => p.id), [1, 3, 2]);
  assert.deepEqual(moverProvedor(lista, 0, 1).map((p) => p.id), [2, 1, 3]);
  assert.deepEqual(moverProvedor(lista, 0, -1).map((p) => p.id), [1, 2, 3]);
  assert.deepEqual(moverProvedor(lista, 2, 1).map((p) => p.id), [1, 2, 3]);
});

test('RNF-REC-001: a cadeia editada difere da gravada pela ordem ou pela ativação', () => {
  const gravada = [provedor(1), provedor(2)];
  assert.equal(cadeiaAlterada(gravada, [provedor(1), provedor(2)]), false);
  assert.equal(cadeiaAlterada(gravada, [provedor(2), provedor(1)]), true);
  assert.equal(cadeiaAlterada(gravada, [provedor(1), provedor(2, false)]), true);
  assert.deepEqual(corpoDaCadeia([provedor(2, false), provedor(1)]), {
    provedores: [{ id: 2, ativo: false }, { id: 1, ativo: true }],
  });
});

test('RNF-REC-001: taxas em porcentagem pt-BR e latência em ms, com travessão quando não há medida', () => {
  assert.equal(formatarTaxa(2 / 3), '66,7%');
  assert.equal(formatarTaxa(1), '100%');
  assert.equal(formatarTaxa(0), '0%');
  assert.equal(formatarTaxa(undefined), '—');
  assert.equal(formatarLatencia(1234), '1.234 ms');
  assert.equal(formatarLatencia(undefined), '—');
  assert.equal(rotuloDoDia('2026-10-04'), '04/10');
  assert.equal(rotuloDoResultado('COTA_ESGOTADA'), 'Cota esgotada');
  assert.equal(rotuloDoResultado('LIMITE_DE_CHAMADAS'), 'Limite de chamadas');
});

test('RNF-REC-001: chamadas HTTP por mensagem com média pt-BR e máximo, ou travessão sem medida', () => {
  assert.equal(formatarChamadasHttp(1.8, 4), '1,8 (máx. 4)');
  assert.equal(formatarChamadasHttp(1, 1), '1,0 (máx. 1)');
  assert.equal(formatarChamadasHttp(2.35, 4), '2,4 (máx. 4)');
  assert.equal(formatarChamadasHttp(undefined, undefined), '—');
});
