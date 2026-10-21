# AGENTS.md

Guia para agentes de IA (Claude, Codex etc.) trabalhando neste repositório.

## Projeto

E-commerce de livros **single-tenant** (uma única loja), trabalho da disciplina LES (Fatec, 2026).
Fonte dos requisitos: DRS v0.6, transcrito em [`docs/requisitos/`](docs/requisitos/README.md) — um arquivo por grupo
(`GER`, `LIV`, `CLI`, `VEN`, `EST`, `ANA`, `REC`). Leia o arquivo do grupo antes de implementar algo dele.

Stack: **a definir**. Ao escolher/introduzir a stack, atualize esta seção (linguagem, framework, banco, comandos de
build/teste/execução).

## Escopo do MVP

O objetivo atual é um MVP. Prioridades:

- **Dentro:** cadastro de clientes (`CLI`: cadastro, alteração, inativação, consulta, endereços, cartões, senha),
  vitrine/consulta de livros (leitura), carrinho e fluxo de venda completo (`VEN`: carrinho com bloqueio por prazo,
  frete, pagamento com múltiplos cartões e cupons, status do pedido, trocas e cupom de troca), baixa/reentrada de
  estoque decorrentes de vendas e trocas (`RF-EST-003`, `RF-EST-004`, `RN-EST-006`), log de transação (`RNF-GER-002`),
  análise de vendas com gráfico e exportação (`ANA`) e recomendação/chatbot com IA generativa (`REC`).
- **Fora (por ora):** CRUD de livros (`RF-LIV-*` de escrita, ativação/inativação) e CRUD/entrada manual de estoque
  (`RF-EST-001`). Livros, estoque e tabelas de domínio (autor, editora, categoria, grupo de precificação, fornecedor,
  bandeiras de cartão etc.) são inseridos via **script SQL de seed** (atende `RNF-LIV-002`).
  As regras de cálculo de preço (`RF-EST-002`, `RN-EST-002`) ainda devem valer sobre os dados do seed.

Na dúvida se algo está no escopo, pergunte em vez de implementar.

## Ambiente

Não há nada em produção. Consequências:

- Não se preocupe com LGPD/GDPR, anonimização ou retenção de dados.
- Migrations não precisam de backfill nem de compatibilidade com dados existentes; pode-se editar/reescrever
  migrations e resetar o banco de desenvolvimento livremente.
- Dados de teste (inclusive cartões fictícios) podem ficar no seed.
- A operadora de cartão (`RN-VEN-007`) e o cálculo de frete (`RF-VEN-004`) podem ser simulados por um serviço fake
  com regras determinísticas.

## Convenções

- Idioma: domínio, requisitos e textos da UI em **português**; identificadores de código seguem a convenção da stack
  (preferência por nomes de domínio em português, consistentes com o DRS: `Cliente`, `Pedido`, `CupomTroca`...).
- Status de pedido exatamente como no DRS: `EM PROCESSAMENTO`, `APROVADA`, `REPROVADA`, `EM TRANSPORTE`, `ENTREGUE`,
  `EM TROCA`, `TROCA AUTORIZADA`, `TROCADO`.
- Regras de negócio ficam na camada de domínio/serviço, não em controllers nem na UI; cada validação deve ser
  testável isoladamente.
- Valores monetários com tipo decimal exato (nunca float); formatação `R$ 1.234,56` na UI.
- Senhas com hash forte (bcrypt/argon2) — `RNF-CLI-003`.
- Referencie o ID do requisito em testes e, quando útil, em comentários (`// RN-VEN-004`).

## Rastreabilidade

Cada arquivo em `docs/requisitos/` tem uma **matriz de rastreabilidade**. Ao implementar (total ou parcialmente) um
requisito, atualize a linha correspondente: status (✅ / 🟡 / ❌), onde no código e observação. Não altere o texto dos
requisitos; se encontrar ambiguidade ou conflito no DRS, registre na coluna Observação e avise.

## Fluxo de trabalho

- Mudanças pequenas e focadas; rode testes antes de concluir.
- Não faça commit/push sem pedido explícito.
