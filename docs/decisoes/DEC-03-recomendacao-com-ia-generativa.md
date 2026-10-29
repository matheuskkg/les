# DEC-03 — Recomendação e chatbot com IA generativa

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-25
- **Card:** #10
- **Requisitos:** RNF-REC-001

## Contexto

RNF-REC-001 pede recomendações personalizadas de livros, a partir do histórico de compras e das preferências, e um
chatbot para busca, dúvidas e sugestões. Pede também que o modelo seja "treinado com dados de vendas e feedback dos
usuários". Restrições:

- Os provedores precisam ter plano gratuito. O ambiente da apresentação é limitado e não roda modelo local.
- As cotas gratuitas são baixas e variam por projeto. Cada chamada à IA conta.
- `mvn test` e os E2E não podem depender de rede nem de chave de API.
- O catálogo vem do seed e é pequeno (20 livros).

Treinar ou fazer fine-tuning de um modelo gratuito não é viável. Por isso, o "treino" é feito pelo **contexto**: cada
chamada leva o histórico, as preferências e o feedback do cliente. A personalização acompanha as compras e o feedback
sem mudar o modelo.

## Decisão

### Biblioteca e provedores

- **Spring AI 2.0.x**, a linha compatível com Spring Boot 4.0/4.1. A 2.1 exige Boot 4.2. Usa-se o cliente **OpenAI**,
  que aceita qualquer endpoint compatível com OpenAI.
- A autoconfiguração do modelo de chat fica desligada. O adapter monta um cliente por provedor a partir de
  `livraria.ia.provedores`, assim a aplicação sobe sem nenhuma chave.
- A conexão de cada provedor é declarada na configuração (`livraria.ia.provedores.<nome>`) com `url-base`, `modelo` e
  `chave`. A chave e o modelo vêm de variáveis de ambiente (alterado na implementação do REC-01: um
  `livraria.ia.tempo-limite`, de 20 s por padrão, vale para todos os provedores, e o cliente não repete a chamada,
  porque a cadeia decide o que fazer na falha; alterado no REC-06: cada item de `livraria.ia.provedores` vira um
  adapter próprio, e a conexão aceita as opções opcionais `tool-choice` (`auto` ou `required`), `formato`
  (`instrucoes` ou `json-schema`), `cabecalhos` (cabeçalhos HTTP extras, como `X-Title` do OpenRouter) e
  `max-chamadas`, com mínimo 1 e padrão 5):

  | Provedor | `url-base` | Chave | Modelo |
  |---|---|---|---|
  | `gemini` | `https://generativelanguage.googleapis.com/v1beta/openai` | `IA_GEMINI_CHAVE` | `IA_GEMINI_MODELO` |
  | `groq` | `https://api.groq.com/openai/v1` | `IA_GROQ_CHAVE` | `IA_GROQ_MODELO` |
  | `openrouter` | `https://openrouter.ai/api/v1` | `IA_OPENROUTER_CHAVE` | `IA_OPENROUTER_MODELO` |

  O modelo é um dos gratuitos do provedor que aceita tool calling e saída estruturada. O `application.yml` traz um
  padrão para cada um, e a variável o troca sem editar arquivos.
- **Cadeia de provedores:** a chamada vai ao primeiro provedor ativo da cadeia que tenha chave. Se ele falhar (erro de rede,
  HTTP 429 ou 5xx, tempo esgotado ou resposta fora do formato), vai ao próximo. Um provedor que respondeu 429 sai da
  cadeia por 5 minutos.
- O provedor `fake` (`AssistenteDeLivrosFake`, determinístico, sem rede) é o último da cadeia. Quando nenhum
  provedor anterior tem chave, como em `mvn test` e nos E2E, a cadeia usa só o `fake`.
- **Troca em tempo de execução:** a ordem e a ativação ficam na tabela `provedor_ia`, com seed `gemini`, `groq`,
  `openrouter` e `fake`, nessa ordem e todos ativos. O ADMIN consulta e altera a ordem e a ativação
  (`GET`/`PUT /api/admin/ia/provedores`). A resposta mostra, por provedor, se ele tem chave e a última falha. A chave
  e a URL ficam só na configuração e nunca são expostas nem alteradas pela API. A cadeia é lida da tabela e guardada
  em memória por 30 segundos; a alteração pelo ADMIN limpa esse cache. Os 5 minutos fora da cadeia depois de um 429
  também ficam só em memória.

### Porta

`AssistenteDeLivros` (`recomendacao/aplicacao/porta`), com duas operações:

| Operação | Entrada | Saída |
|---|---|---|
| `recomendar` | perfil do cliente e candidatos | ids dos livros em ordem, cada um com uma justificativa curta |
| `conversar` | perfil do cliente, últimas mensagens e mensagem nova | `tipo`, texto da resposta e ids dos livros citados |

As duas respostas são **saída estruturada** (JSON validado pelo Spring AI), nunca texto livre interpretado pelo
server.

### Recuperação (RAG estruturado)

Os dados enviados à IA são recuperados pelo server com regras determinísticas. Não há busca vetorial.

- **Perfil do cliente:** os livros comprados (compras que contam como venda, como no ranking do
  [DEC-04](DEC-04-ranking-de-cliente.md)), as categorias e os autores mais frequentes e as avaliações dadas aos itens
  recomendados.
- **Candidatos da vitrine:** livros ativos com estoque disponível ([DEC-15.3](DEC-15.3-saldo-de-estoque.md)), sem os
  já comprados e sem os que tiveram uma recomendação avaliada negativamente. A pontuação soma as categorias e os
  autores em comum com os livros comprados e com os recomendados avaliados positivamente, e a co-compra ("quem
  comprou X também comprou Y"). Os 8 de maior pontuação vão à IA. Sem histórico, os mais vendidos.
- **Chat:** a IA recebe o perfil e tem **tools só de leitura**: buscar livros (por texto em título, autor ou sinopse,
  categoria e faixa de preço), ver um livro, ver o próprio histórico e `recomendarParaMim` (com os mesmos candidatos
  da vitrine). As tools usam os casos de uso do `catalogo`, do `estoque` e do `vendas`. O id do cliente vem sempre do
  `UsuarioAtual`, nunca de argumento do modelo.

### Conversa

- Cada cliente tem **uma conversa contínua** (`conversa` e `mensagem_conversa`), retomada em qualquer acesso. As
  mensagens guardam o papel (`CLIENTE` ou `ASSISTENTE`), o texto, o tipo da resposta e os livros citados
  (`mensagem_livro`).
- "Limpar conversa" preenche `arquivada_em`, e a próxima mensagem abre uma conversa nova. A conversa arquivada fica no
  banco e não é exibida a ninguém.
- O histórico enviado à IA é lido do banco (as 10 últimas mensagens da conversa ativa), nunca recebido na requisição.
  Assim, o cliente não consegue forjar respostas do assistente.

### Proteções

Uma regra por arquivo, testável sem a IA:

1. **Antes da chamada:** a mensagem tem no máximo 500 caracteres, e o cliente envia no máximo 10 mensagens por minuto
   (contadas em `mensagem_conversa`).
2. **Uma única chamada por mensagem:** o prompt de sistema restringe o assunto à livraria e ao catálogo. A resposta
   traz `tipo` (`RECOMENDACAO`, `BUSCA`, `DUVIDA` ou `FORA_DO_ESCOPO`). Com `FORA_DO_ESCOPO`, o server grava como
   resposta um texto fixo e descarta o texto do modelo. (Alterado depois da rodada de testes do chat: a mensagem pode
   gerar mais de uma chamada ao mesmo provedor quando há motivo, que são as voltas do loop de tools, até 3, e uma
   correção de formato, que reenvia ao provedor a resposta inválida com o erro. O total fica dentro de
   `livraria.ia.provedores.<nome>.max-chamadas`, porque os limites gratuitos variam por provedor. Passar do limite
   conta como falha do provedor, e a cadeia segue para o próximo. O classificador de assunto separado continua
   descartado.)
3. **Depois da chamada:** um livro citado que não existe, está inativo ou sem estoque é removido da resposta. Na
   vitrine, só valem os ids que estavam entre os candidatos.
4. (Acrescentado depois da rodada de testes do chat) **No chat, a recomendação usa os mesmos candidatos da vitrine:**
   a tool `recomendarParaMim` recupera e pontua os livros como a vitrine. Com o tipo `RECOMENDACAO`, os livros que o
   cliente já comprou são removidos da resposta; `BUSCA` e `DUVIDA` os mantêm, porque o cliente pode perguntar de um
   livro que já tem.
5. A IA não executa ações: adicionar ao carrinho é um botão da UI sobre os livros devolvidos.

### Recomendação da vitrine

- Cada geração grava uma `recomendacao` do cliente com os itens (`item_recomendacao`: livro, posição e
  justificativa). A vitrine mostra a mais recente.
- Ela é reaproveitada até a próxima compra do cliente ou por 24 horas. Passado isso, a próxima visita gera outra. (A compra é contada pelo número de pedidos do cliente que contam como venda, para um pedido aprovado depois
  da geração também a invalidar; a gravada sem IA é reaproveitada por 1 hora, para a IA ser tentada de novo logo.)
- Se toda a cadeia falhar, a recomendação é gravada com os candidatos na ordem da pontuação, sem justificativa e sem
  chamada associada.

### Avaliação das respostas

- O cliente avalia cada item recomendado e cada resposta do assistente como positiva ou negativa (`avaliacao` e
  `avaliada_em` em `item_recomendacao` e `mensagem_conversa`). A última avaliação vale.
- Um item avaliado negativamente some da vitrine na hora, sem gerar outra recomendação, e o livro sai dos candidatos
  seguintes. As avaliações entram no perfil e na taxa de aprovação das métricas.

### Registro das chamadas

- Toda tentativa de chamada a um provedor, com sucesso ou falha, grava uma linha em `chamada_ia`: provedor, modelo,
  operação (`RECOMENDAR` ou `CONVERSAR`), início, latência em ms, número da tentativa na cadeia (1 = primeiro
  provedor tentado), resultado (`SUCESSO`, `COTA_ESGOTADA`, `ERRO_HTTP`, `TEMPO_ESGOTADO`, `ERRO_DE_REDE` ou
  `RESPOSTA_INVALIDA`), código HTTP, mensagem de erro (até 500 caracteres) e tokens de entrada e saída, quando o
  provedor informa.
- O prompt e a resposta crus não são gravados. O conteúdo fica nas mensagens e nos itens recomendados.
  (Alterado no REC-06: cada falha também gera um `WARN` no log do server, com provedor, modelo, resultado, código
  HTTP e mensagem truncada, sem a chave nem o prompt/mensagens do cliente.)
- A resposta do assistente e a recomendação apontam para a chamada que as gerou (`chamada_ia_id`).
- `chamada_ia` é um log só de inserção e não tem tabela `_aud`. As demais tabelas do módulo são auditadas
  (RNF-GER-002).

### Métricas

Tela do ADMIN junto da configuração dos provedores (`GET /api/admin/ia/metricas?inicio=&fim=`), calculada sobre
`chamada_ia` no período, por provedor:

- chamadas e taxa de sucesso;
- falhas por resultado e a última falha;
- latência média e p95 das chamadas com sucesso;
- tokens de entrada e saída;
- atendimentos por reserva (sucessos com tentativa maior que 1) e recomendações sem IA;
- taxa de aprovação (avaliações positivas sobre as avaliadas).

Um gráfico (`uplot`) mostra as chamadas e as falhas por dia.

### Web

- **Widget do assistente** (módulo `assistente`), montado no `app.tsx` fora das rotas, em todas as telas da loja e
  fora das telas do admin:
  - botão flutuante no canto inferior direito, que abre um painel (cerca de 380 × 560 px; tela cheia abaixo de
    640 px). Aberto ou fechado, o estado segue na navegação e fica guardado na aba (`sessionStorage`);
  - para o visitante, o painel convida a entrar e leva ao login, voltando à mesma página depois;
  - ao abrir, carrega a conversa ativa. Sem mensagens, mostra sugestões de perguntas que enviam a mensagem ao
    clicar;
  - sem streaming: uma requisição por mensagem. A mensagem do cliente aparece na hora, um indicador de "digitando"
    fica até a resposta e o envio fica desabilitado. Enter envia, Shift+Enter quebra a linha, e um contador mostra o
    limite de 500 caracteres;
  - os livros citados aparecem como cartões com capa, preço, link para o detalhe e botão de adicionar ao carrinho;
  - cada resposta tem botões de avaliação positiva e negativa, e o cabeçalho do painel tem "Limpar conversa";
  - o 503 mostra "assistente indisponível" com "tentar de novo", e o 422 mostra as violações;
  - acessibilidade: o painel é um `dialog` não modal com título, as respostas novas são anunciadas (`aria-live`),
    Esc fecha e devolve o foco ao botão.
- **Vitrine:** seção "Recomendados para você" no topo do catálogo para o cliente logado, com a justificativa de cada
  livro e os botões de avaliação. Sem recomendação, a seção não aparece.
- **Admin:** aba "Assistente" com a cadeia de provedores (ordem, ativação, chave configurada, última falha) e as
  métricas.

## Alternativas consideradas

- RAG vetorial (embeddings das sinopses): sem ganho com 20 livros. O pgvector não existe no H2, embeddings remotos
  gastam cota e embeddings locais aumentam o jar.
- Catálogo inteiro no prompt: mais simples, mas não usa o histórico como recuperação e gasta tokens a cada mensagem.
- Classificador de assunto numa chamada separada: dobra o consumo da cota. A saída estruturada com
  `FORA_DO_ESCOPO` resolve com uma chamada.
- Modelo local (Ollama): o ambiente da apresentação não comporta.
- Um provedor só: uma cota esgotada derruba o assistente durante a apresentação.
- Ordem dos provedores só em memória: a alteração do ADMIN se perderia ao reiniciar.
- Cache da vitrine em memória: um reinício gastaria cota para gerar de novo, e a avaliação não teria a que apontar.
- Várias conversas por cliente, com lista: mais tela sem ganho para o requisito.
- Histórico da conversa enviado pelo web: o cliente poderia forjar respostas do assistente.
- Gravar o prompt e a resposta crus: tabela pesada; o conteúdo útil já está nas mensagens.
- Avaliação por livro, independente da resposta: não mede a qualidade da IA nas métricas.
- LangChain4j: o starter para Spring Boot 4 ainda está em beta.
- `RestClient` sem biblioteca: function calling e saída estruturada ficariam por conta do projeto.
- Streaming por SSE: a saída estruturada só serve completa.

## Consequências

- Dependência nova no `server`: o starter OpenAI do Spring AI 2.0.x, com a autoconfiguração do chat desligada.
- Módulo `recomendacao` com a porta `AssistenteDeLivros`, o adapter fake, os adapters por provedor e a cadeia, as
  tabelas `provedor_ia`, `chamada_ia`, `conversa`, `mensagem_conversa`, `mensagem_livro`, `recomendacao` e
  `item_recomendacao`, e os endpoints de vitrine, chat, avaliação, provedores e métricas.
- Seed `500_provedores_ia.sql` com a cadeia padrão.
- Os testes usam o fake. Os adapters reais são testados com um servidor HTTP local que imita a API OpenAI, inclusive
  429 e resposta inválida.
- Para a apresentação, basta definir as variáveis `IA_*_CHAVE` dos provedores desejados, como explica o guia
  [`configuracao-da-ia.md`](../guias/configuracao-da-ia.md).
