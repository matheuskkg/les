# DEC-22 — Lint e formatação do server

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-11-22

## Contexto

O `server` não tinha nenhuma ferramenta de lint ou formatação: o build contava só com compilador, testes e ArchUnit.
Na prática, agentes de IA commitavam arquivos com imports não usados, e não havia uma forma automática de detectar
variáveis órfãs ou bugs simples. O `javac` não avisa sobre imports/variáveis não usados, e o ArchUnit verifica só
arquitetura.

## Decisão

Adotar duas ferramentas no build Maven do `server`:

- **Spotless 3.12.0** com `googleJavaFormat` (estilo AOSP, 120 colunas, `reorderImports`), `removeUnusedImports`,
  `trimTrailingWhitespace` e `endWithNewline`. O goal `apply` fica amarrado à fase `process-sources`, então até o
  `mvn test` corrige o código antes de compilar — imports não usados nunca chegam a um commit feito pelo fluxo normal.
  AOSP foi escolhido para manter a indentação de 4 espaços já usada no projeto; 120 colunas evita quebras curtas.
- **SpotBugs 4.10.5.0** com `effort=Default`, `threshold=High` e goal `check` na fase `verify`, falhando o build quando
  encontra bugs. Falsos positivos são exceções explícitas em `apps/server/spotbugs-exclude.xml`, cada uma com
  justificativa. A primeira exceção registra que `RecusarTroca` recebe `motivo` anulável por contrato e valida com
  `MotivoDaRecusaInformado` (RF-VEN-012) antes do `Objects.requireNonNull`.

## Alternativas consideradas

- **PMD** (regras `UnusedLocalVariable`, `UnusedPrivateField`, `UnusedPrivateMethod`) — complementaria o Spotless ao
  detectar variáveis não usadas, que nem o Spotless nem o SpotBugs veem. Fica para uma segunda passada, para introduzir
  menos ruído de uma vez.
- **Checkstyle** — só reporta imports não usados, sem corrigi-los; o Spotless já corrige automaticamente.
- **Palantir Java Format** — quebra menos linhas, mas é uma mudança visual maior que o AOSP para o código existente.

## Consequências

- O primeiro `spotless:apply` reformatou 853 arquivos Java; a partir daí o formato é estável e idempotente.
- `mvn verify` passa a rodar formatação, testes e análise estática em sequência. `mvn test` continua só com testes, mas
  já aplica a formatação.
- Variáveis não usadas **ainda não** são detectadas automaticamente; essa lacuna será fechada se o PMD for adotado.
