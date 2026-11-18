# DEC-19 — Filtro da análise de vendas por datas exatas

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-11-17
- **Requisitos:** RF-ANA-001, RF-ANA-002, RN-ANA-001, RN-ANA-002, RNF-ANA-001

## Contexto

O DRS pede que o administrador informe a "data de início e fim" da análise (RF-ANA-001 e RF-ANA-002), mas as regras
de negócio e a implementação acabaram fixando a granularidade mensal: o filtro usava `AAAA-MM`, o intervalo ia de 1 a
24 meses (RN-ANA-002) e o eixo X do gráfico mostrava `mm/aaaa` (RNF-ANA-001). O resultado é que o administrador não
consegue recortar as vendas por dia.

Há três problemas a resolver ao aceitar datas exatas: o que aparece no eixo X, como fica o limite de 24 meses e o que
acontece com os meses das bordas do intervalo.

## Decisão

Os campos da tela passam a ser `date` e a API recebe `inicio` e `fim` em `AAAA-MM-DD`, ambos inclusivos. O filtro da
consulta ao banco usa o primeiro instante da data inicial e o último instante da data final, então um recorte como
12/03 a 20/03 traz só as vendas desses nove dias.

O gráfico e a tabela continuam com um ponto por mês. O eixo X segue os meses tocados pelo intervalo, de modo que
12/03 a 20/03 produz um único ponto, rotulado `03/2025`, com o valor vendido nesses nove dias. O mesmo vale para a
exportação: a coluna `mes` segue em `AAAA-MM`, mas uma borda parcial traz apenas as vendas dos dias selecionados.

O limite de 24 meses é contado por aritmética de meses, não por dias. A quantidade é
`YearMonth(from(inicio)).until(YearMonth(from(fim)), MONTHS) + 1`, que dá o número de meses calendário tocados. Assim,
29/02/2024 a 28/02/2025 conta 12 meses, e dois dias em meses diferentes já contam dois meses. O limite continua sendo
o mesmo da RN-ANA-002; o que muda é a forma de medi-lo.

O cabeçalho da tela mostra as datas exatas no formato `dd/mm/aaaa`, e o nome do arquivo CSV passa a usar `AAAA-MM-DD`
nas duas pontas.

## Alternativas consideradas

- Um ponto por dia no gráfico. Mostraria o filtro com mais clareza, mas contraria a RN-ANA-001 e a RNF-ANA-001 e
  deixaria o gráfico com até centenas de pontos, difícil de ler na apresentação.
- Limitar o período contando o total de dias. É a conversão mais direta de 24 meses, mas o número de dias entre duas
  datas depende de anos bissextos e do tamanho de cada mês; 29/02/2024 a 28/02/2025 tem 366 dias e ainda assim é um
  ano.
- Escolher a granularidade conforme o intervalo, com dias nos recortes curtos e meses nos longos. Daria mais
  flexibilidade, mas exigiria dois formatos de eixo, duas regras de exportação e mais explicação na apresentação.

## Consequências

A API troca o formato de `inicio` e `fim` nos dois endpoints de análise, e o contrato OpenAPI é regenerado. No
domínio, `SolicitacaoDeAnalise` e `PeriodoDeAnalise` passam a guardar `LocalDate`; `VendaNoMes`, `AnaliseDeVendas` e
`PlanilhaDaAnalise` permanecem com `YearMonth`, porque a agregação continua mensal. A regra
`FimNaoAnteriorAoInicio` passa a falar em data, e a mensagem de erro muda junto.

Os testes de controller passam a usar datas e ganham um caso que consulta dois recortes dentro de março de 2025. O
roteiro de apresentação da análise troca os rótulos dos campos e inclui um cenário em que o administrador compara
05/03 com 12/03 a 20/03. A matriz de rastreabilidade registra a extensão do DRS na observação da RF-ANA-002 e da
RN-ANA-002.
