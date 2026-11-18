# DEC-20 — Janela e densidade do histórico de seed

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-11-17
- **Card:** ALT-05
- **Requisitos:** RNF-LIV-002, RF-ANA-001, RN-ANA-002
- **Substitui:** apenas o item "Pedidos históricos" do [DEC-14](DEC-14-ids-nomes-e-seed.md), quanto à janela e ao volume; IDs, nomes, divisão dos scripts e regras de sequence do DEC-14 permanecem em vigor.

## Contexto

O [DEC-14](DEC-14-ids-nomes-e-seed.md) fixou "cerca de 24 meses, de novembro de 2024 a outubro de 2026" para os pedidos históricos inseridos pelo seed. A RN-ANA-002 limita a **janela consultada** do gráfico a no máximo 24 meses, e o E2E de apresentação usa o histórico completo dentro desse limite. O pedido de ampliação da massa de seed levantou a dúvida: aquele limite da regra impediria o banco de guardar mais de 24 meses?

Não impede. A RN-ANA-002 fala do intervalo informado pelo administrador na análise (RF-ANA-002); nada no DRS restringe o período armazenado. Mantiveram-se, portanto, a regra e a consulta da análise — o que muda é apenas a quantidade de dados disponíveis para consultar.

## Decisão

O seed `400_pedidos_historicos.sql` passa a cobrir **36 meses, de novembro de 2023 a outubro de 2026**:

- Os 24 meses finais (nov/2024 a out/2026), inclusive todos os pedidos, trocas, IDs e valores já conferidos por testes e pelo E2E, ficam **integralmente preservados**.
- Os 12 meses novos (nov/2023 a out/2024) recebem de 16 a 22 pedidos por mês, nenhum mês vazio.
- A densidade dos 17 meses sem valor fixado dentro dos 24 finais também foi aumentada.
- O gerador usa três sementes fixas e independentes (`2026`, `2027`, `2030`), uma para cada laço, para que acrescentar meses não desloque os sorteios anteriores.
- A massa adicional é de clientes novos (6 a 10); a Carla (3) e a Elisa (5) mantêm o histórico original, e a Ana e o Bruno continuam sem pedidos, como os testes esperam.

Números resultantes: 847 pedidos, 1524 itens, 98 trocas, 63 cupons de troca, 36 meses com vendas e sete clientes com histórico. A janela da análise continua limitada a 24 meses pela RN-ANA-002, sem alteração de código, contrato ou spec de E2E.

## Alternativas consideradas

- **Reduzir o histórico para 12 meses.** Contradiz o pedido de janela maior e empobrece o gráfico de apresentação.
- **Estender além de 36 meses.** Aumentaria o arquivo sem ganho de clareza na tela; 36 meses já dão folga para escolher qualquer recorte de até 24 meses.
- **Mudar a RN-ANA-002 para 36 meses.** A regra define o recorte da análise, não o armazenamento; alterá-la modificaria requisito e API sem necessidade.
- **Reaproveitar a Carla e a Elisa nos meses novos.** Mudaria os valores de ranking e de pedidos que testes e E2E conferem.

## Consequências

- Os testes de seed passam a conferir contagens e datas exatas, incluindo a quantidade de pedidos mês a mês na janela estendida.
- As matrizes de RNF-LIV-002, RF-ANA-001 e RF-VEN-015 registram a nova janela e o novo volume.
- Bancos Postgres já existentes precisam ser recriados: o `ExecutorDeSeeds` registra checksum e não reexecuta arquivos alterados. Em H2 o schema é recriado a cada inicialização.
- Nenhuma entidade, regra de negócio, endpoint ou contrato OpenAPI muda.
