# DEC-06 — Data de validade no cartão de crédito

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-21
- **Requisitos:** RN-CLI-004 (estende), RF-CLI-007, RN-VEN-007

## Contexto

RN-CLI-004 compõe o cartão com número, nome impresso, bandeira e código de segurança, sem data de validade. Sem ela,
a operadora fake ([DEC-05.2](DEC-05.2-operadora-de-cartao.md)) não consegue simular a validação mais comum de um cartão
real.

## Decisão

O cartão passa a ter também **data de validade**, obrigatória, no formato mês/ano (`MM/AA` na UI, `YearMonth` no
domínio). O cartão vale até o último dia do mês informado.

- **Cadastro e alteração do cartão (RF-CLI-007):** rejeitar validade já vencida no mês corrente.
- **Finalização da compra (RN-VEN-007):** a operadora fake reprova o cartão vencido na data da transação, pois ele
  pode ter vencido depois de cadastrado.

## Alternativas consideradas

- Seguir o DRS sem validade: a operadora fake ficaria restrita a regras arbitrárias sobre o número e o CVV.

## Consequências

- É uma extensão do DRS: o texto de RN-CLI-004 não muda, e a matriz registra o campo a mais na Observação.
- Todo cartão, inclusive os de teste, tem validade.
