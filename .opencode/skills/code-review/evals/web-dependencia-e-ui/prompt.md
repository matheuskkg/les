---
tags: [code-review]
max_turns: 20
timeout_seconds: 600
allowed_tools: [Read, Glob, Grep, Skill, Write, Edit, Bash]
---

Repositório: /home/matheus/Projects/Fatec/LES. O diff abaixo é o escopo da revisão (não está aplicado no working tree; o repo serve para consultar as instruções e o código ao redor). Revise e me entregue só o relatório na conversa, sem editar arquivos.

Arquivo: apps/web/package.json (trecho adicionado)

+ "dependencies": {
+   "preact": "^10.0.0",
+   "preact-iso": "^2.0.0",
+   "uplot": "^1.6.0",
+   "zustand": "^5.0.0"
+ }

Arquivo: apps/web/src/ui/PrecoLivro.tsx (novo)

import { buscarLivro } from '../modulos/catalogo/api';
import { useCarregamento } from '../compartilhado/hooks/useCarregamento';
import type { Livro } from '../api/esquema';

export function PrecoLivro({ id }: { id: number }) {
  const { dados } = useCarregamento(() => buscarLivro(id));
  const total = Number(dados.precoVenda) * 2;
  return <span>R$ {total.toFixed(2)}</span>;
}
