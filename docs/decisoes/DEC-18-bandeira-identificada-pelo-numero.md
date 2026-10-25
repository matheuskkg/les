# DEC-18 — Bandeira do cartão identificada pelo número

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-24
- **Requisitos:** RN-CLI-005, RN-CLI-004 (estende), RF-CLI-007, RF-VEN-005

## Contexto

O RN-CLI-005 exige que o cartão seja de alguma bandeira registrada, mas não diz como a bandeira do cartão é
determinada. Hoje o cliente escolhe a bandeira num select no cadastro, nos cartões da conta e no "outro cartão" do
checkout ([DEC-10](DEC-10-cliente-enderecos-e-cartoes.md)). O select deixa registrar uma bandeira incoerente com o
número (um número Visa sob a bandeira Mastercard) e pede uma ação que o próprio número já determina.

## Decisão

A bandeira do cartão **é identificada pelo número** no back-end. O campo `bandeiraId` sai de todas as entradas da API
(cadastro e alteração de cartão, cartões da conta e o "outro cartão" do pagamento); as respostas continuam trazendo a
bandeira, que o front pode exibir apenas como informação. No banco, o `cartao.bandeira_id`
([DEC-16](DEC-16-modelo-de-dados.md)) é gravado com a bandeira identificada.

A tabela `bandeira` (módulo `clientes`, seed `200_bandeiras.sql`) tem duas colunas:

- `prefixos`: prefixos ou faixas inclusivas `ini-fim`, separados por vírgula;
- `tamanhos`: comprimentos de número aceitos, separados por vírgula.

| Bandeira | `prefixos` | `tamanhos` |
|---|---|---|
| Visa | 4 | 13,16,19 |
| Mastercard | 51-55,2221-2720 | 16 |
| Elo | 401178,401179,431274,438935,451416,457393,457631,457632,504175,506699-506778,509000-509999,627780,636297,636368,650031-650033,650035-650051,650405-650439,650485-650538,650541-650598,650700-650718,650720-650727,650901-650978,651652-651679,655000-655019,655021-655058 | 16 |
| American Express | 34,37 | 15 |
| Hipercard | 606282,3841 | 16,19 |

- A bandeira é a cujos `prefixos` o início do número casa (prefixo puro ou faixa `ini-fim` inclusiva). **Vence o
  prefixo mais longo**: o `438935` casa com o prefixo de 6 dígitos da Elo antes do `4` da Visa, e é Elo.
- Número sem bandeira correspondente é recusado com "Bandeira do cartão não aceita." (RN-CLI-005).
- Havendo bandeira, o comprimento do número deve estar em `tamanhos` e o dígito verificador deve passar pelo
  algoritmo de Luhn; do contrário, o cartão é recusado com "Número do cartão inválido." — validação que complementa a
  composição do RN-CLI-004.

## Alternativas consideradas

- Manter o select: exige uma ação que o número já determina e permite registrar uma bandeira incoerente com o número.
- Prefixos como constante no código: separa o cadastro da bandeira da regra que a identifica; na tabela do seed,
  uma bandeira nova é uma linha do script (domínios por seed, RNF-LIV-002).
- Identificar só no front: regra de negócio fica no back-end, e o RN-CLI-005 não pode depender da UI.

## Consequências

- `200_bandeiras.sql` ganha as colunas com os valores da tabela; o modelo
  (`conceitual.spec.json`, `mer.spec.json`) e o diagrama gerado refletem os atributos novos.
- A identificação da bandeira pelo número, o tamanho conforme a bandeira e o Luhn são regras isoladas em arquivos
  próprios, cada uma testável independentemente.
- As três telas deixam de enviar `bandeiraId`; a bandeira detectada aparece só como informação.
- Matriz de rastreabilidade: RN-CLI-004 registra a extensão (tamanho e Luhn) e RN-CLI-005, a identificação pelo
  número.
