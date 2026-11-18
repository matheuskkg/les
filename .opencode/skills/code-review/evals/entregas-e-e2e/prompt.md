---
tags: [code-review]
max_turns: 20
timeout_seconds: 600
allowed_tools: [Read, Glob, Grep, Skill, Write, Edit, Bash]
---

Repositório: /home/matheus/Projects/Fatec/LES. O diff abaixo é o escopo da revisão (não está aplicado no working tree; o repo serve para consultar as instruções e o código ao redor). Revise e me entregue só o relatório na conversa, sem editar arquivos.

Arquivo: docs/entregas/dvp/04-declaracao-preliminar-de-escopo.md (trecho adicionado)

+ O escopo do projeto está a definir. Veja mais em docs/requisitos/vendas.md e no arquivo apps/web/src/app.tsx.

Arquivo: apps/web/e2e/apresentacaoAnalise.spec.ts (novo)

test('valida botao', async ({ page }) => {
  await page.goto('/admin/analise');
  await expect(page.getByRole('button', { name: 'Atualizar' })).toBeDisabled();
  const resposta = await page.request.get('/api/admin/analise');
  expect(resposta.status()).toBe(200);
});
