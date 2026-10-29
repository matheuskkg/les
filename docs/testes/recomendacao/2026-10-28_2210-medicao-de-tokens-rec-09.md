# Medição de tokens do chat no REC-09 — 2026-10-28 22:10 (BRT)

Medição do ganho de tokens do contexto compacto do chat (REC-09, DEC-03, RNF-REC-001), feita com os registros por
volta de `chamada_ia` do REC-07. Mesmos prompts e mesma cliente nas duas rodadas, uma antes e outra depois do REC-09.

## Ambiente

| Item | Valor |
|---|---|
| Antes | `master` em `6b19792` (REC-06, REC-07 e REC-08; perfil completo no prompt e tools de busca) |
| Depois | branch `rec-09` (catálogo compacto e perfil enxuto no prompt; tools `verLivro`, `recomendarParaMim` e `responder`) |
| Server | `mvn spring-boot:run` (profile `h2`), porta 18080, só o Gemini com chave (`IA_GROQ_CHAVE` e `IA_OPENROUTER_CHAVE` vazias) |
| Modelo | `gemini-3.5-flash-lite`, formato `ferramenta` (padrão) |
| Cliente | Elisa Prado (`elisa.prado@livraria.test`): 127 pedidos no seed, já comprou 18 dos 20 livros |
| Intervalo entre mensagens | 15 s, começando com "Limpar conversa" |
| Fonte dos números | `GET /api/admin/ia/metricas` do dia (soma de `chamada_ia` do provedor) |

## Prompts

| ID | Prompt |
|---|---|
| C01 | Oi! O que você consegue fazer por mim? |
| C03 | Quero um livro de ficção científica. Quais vocês têm? |
| C04b | Vocês têm Dom Casmurro? |
| C05b | Quanto custa O Hobbit e tem em estoque? |
| C02 | Com base no que eu já comprei, o que você me recomenda ler agora? |

## Resultado

| Medida | Antes | Depois | Diferença |
|---|---|---|---|
| Mensagens atendidas pelo Gemini | 4 de 5 | 5 de 5 | |
| Chamadas HTTP | 13 | 7 | −46% |
| Chamadas HTTP por mensagem (média / máximo) | 2,6 / 4 | 1,4 / 2 | |
| Tokens de entrada | 18.671 | 9.536 | −49% |
| Tokens de saída | 414 | 353 | −15% |
| Falhas | 1 `LIMITE_DE_CHAMADAS` | 0 | |

Antes, o C03 passou de 3 voltas de tools, porque o modelo buscava o catálogo aos poucos. Com isso foi atendido pelo
`fake`, em 17,6 s. Depois, o C03 foi respondido numa chamada com os 5 livros de ficção científica do seed, e o C04b e
o C05b também citaram o livro, o que antes não acontecia. O C02 não citou livros em nenhuma das rodadas, porque a
cliente já comprou quase todo o catálogo. Esse cenário fica para a rodada do REC-10, com uma cliente de histórico curto.

## Limitações

- Uma rodada por versão, só com o Gemini. A contagem de tokens varia por provedor (no relatório de 2026-10-27, o
  OpenRouter somou cerca de 16 mil tokens de entrada por chamada).
- A cliente tem histórico longo, que é o caso em que o perfil enxuto mais reduz o contexto.
