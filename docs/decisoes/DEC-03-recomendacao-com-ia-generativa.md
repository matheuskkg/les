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
  `max-chamadas`, com mínimo 1 e padrão 5; alterado no REC-07: o `formato` padrão passa a ser `ferramenta`, com
  `json-schema` e `instrucoes` como alternativas, e `max-chamadas` limita todas as chamadas HTTP da mensagem, inclusive
  as voltas de tools e a correção de formato; alterado em ALT-08: a conexão aceita também `max-tokens`, o teto de
  tokens de saída por chamada, com 0 (o padrão) ou omitido significando sem teto, o negativo recusado e qualquer valor
  a partir de 1 servindo de teto — sem o campo, o plano gratuito do Groq reserva a saída pedida contra o limite por
  minuto (OTPM de 1.000) e responde 429 antes de escrever qualquer texto, e o despejo de catálogo da rodada de 22/11
  chegou a 3.364 tokens de saída, então o `application.yml` define 800 no `gemini` e no `groq` e 1.200 no `openrouter`
  e no `nvidia`; o trade-off é que uma resposta cortada no meio vira JSON inválido, o que a correção de formato e a
  cadeia, que já existem, cobrem):

  | Provedor | `url-base` | Chave | Modelo |
  |---|---|---|---|
  | `gemini` | `https://generativelanguage.googleapis.com/v1beta/openai` | `IA_GEMINI_CHAVE` | `IA_GEMINI_MODELO` |
  | `groq` | `https://api.groq.com/openai/v1` | `IA_GROQ_CHAVE` | `IA_GROQ_MODELO` |
  | `openrouter` | `https://openrouter.ai/api/v1` | `IA_OPENROUTER_CHAVE` | `IA_OPENROUTER_MODELO` |
  | `nvidia` | `https://integrate.api.nvidia.com/v1` | `IA_NVIDIA_CHAVE` | `IA_NVIDIA_MODELO` |

  (alterado no REC-10: `nvidia` (NVIDIA NIM) entrou na cadeia depois de passar no teste de fumaça com tools e saída
  JSON; o Mistral foi descartado porque o plano gratuito não permite criar chave de API)

  O modelo é um dos gratuitos do provedor que aceita tool calling e saída estruturada. O `application.yml` traz um
  padrão para cada um, e a variável o troca sem editar arquivos (alterado em ALT-11, 2026-11-22: o padrão do
  OpenRouter passou de `google/gemma-4-31b-it:free` — sistematicamente recusado pelo pool gratuito do upstream com
  429 "Provider returned error" mesmo com a cota diária da conta zerada — para
  `nvidia/nemotron-3.5-super-vl:free`, validado com tools e saída estruturada; o 429 do pool é independente da cota
  de 50 chamadas free/dia da conta, e o contador diário aparece em `GET /api/v1/key`).
- **Cadeia de provedores:** a chamada vai ao primeiro provedor ativo da cadeia que tenha chave. Se ele falhar (erro de rede,
  HTTP 429 ou 5xx, tempo esgotado ou resposta fora do formato), vai ao próximo. Um provedor que respondeu 429 sai da
  cadeia por 5 minutos. (Alterado no REC-09: a espera segue o `Retry-After` ou o tempo citado na mensagem, entre 10 s
  e 5 min, e, na cota diária, vai até a meia-noite; sem indicação, 5 min.)
- O provedor `fake` (`AssistenteDeLivrosFake`, determinístico, sem rede) é o último da cadeia, mas fica **inativo por
  padrão** no seed. O profile `dev` e os testes o ativam por um seed próprio. Na apresentação, só os provedores reais
  ativos atendem, e uma falha deles não é mascarada pelo fake; sem chave e sem o profile `dev`, o assistente fica
  indisponível.
- **Troca em tempo de execução:** a ordem e a ativação ficam na tabela `provedor_ia`, com seed `gemini`, `groq`,
  `openrouter` e `fake` (alterado no REC-10: a ordem passa a ser `gemini`, `groq`, `openrouter`, `nvidia` e `fake`;
  alterado depois: os provedores reais ficam ativos e o `fake`, inativo). O ADMIN consulta e altera a ordem e a ativação
  (`GET`/`PUT /api/admin/ia/provedores`). A resposta mostra, por provedor, se ele tem chave e a última falha. A chave
  e a URL ficam só na configuração e nunca são expostas nem alteradas pela API. A cadeia é lida da tabela e guardada
  em memória por 30 segundos; a alteração pelo ADMIN limpa esse cache. Os 5 minutos fora da cadeia depois de um 429
  também ficam só em memória. (Alterado no REC-09: o prazo variável, inclusive até a meia-noite na cota diária,
  continua só em memória.)

### Porta

`AssistenteDeLivros` (`recomendacao/aplicacao/porta`), com duas operações:

| Operação | Entrada | Saída |
|---|---|---|
| `recomendar` | perfil do cliente e candidatos | ids dos livros em ordem, cada um com uma justificativa curta |
| `conversar` | perfil do cliente, últimas mensagens e mensagem nova | `tipo`, texto da resposta e ids dos livros citados |

As duas respostas são **saída estruturada** (JSON validado pelo Spring AI), nunca texto livre interpretado pelo
server. (Alterado no REC-07: no chat com `formato: ferramenta`, a resposta final vem da tool `responder`; nos demais
formatos, continua sendo o JSON do texto. Alterado no REC-09: `conversar` também recebe as ferramentas do cliente e o
catálogo à venda, que vai compacto ao prompt.)

### Recuperação (RAG estruturado)

Os dados enviados à IA são recuperados pelo server com regras determinísticas. Não há busca vetorial.

- **Perfil do cliente:** os livros comprados (compras que contam como venda, como no ranking do
  [DEC-04](DEC-04-ranking-de-cliente.md)), as categorias e os autores mais frequentes e as avaliações dadas aos itens
  recomendados. (Alterado no REC-09: no chat, o perfil leva só os ids dos livros comprados que estão no catálogo, o
  título dos demais, até 5 categorias e autores e os ids das avaliações; a vitrine continua com a descrição completa.)
- **Candidatos da vitrine:** livros ativos com estoque disponível ([DEC-15.3](DEC-15.3-saldo-de-estoque.md)), sem os
  já comprados e sem os que tiveram uma recomendação avaliada negativamente. A pontuação soma as categorias e os
  autores em comum com os livros comprados e com os recomendados avaliados positivamente, e a co-compra ("quem
  comprou X também comprou Y"). Os 8 de maior pontuação vão à IA. Sem histórico, os mais vendidos.
- **Chat:** a IA recebe o perfil e tem **tools só de leitura**: buscar livros (por texto em título, autor ou sinopse,
  categoria e faixa de preço), ver um livro, ver o próprio histórico (acrescentado depois da rodada de testes do chat:
  e `recomendarParaMim`, com os mesmos candidatos da vitrine). As tools usam os casos de uso do `catalogo`, do `estoque` e do `vendas`. O id do cliente vem sempre do
  `UsuarioAtual`, nunca de argumento do modelo. (Alterado no REC-07: no formato `ferramenta`, o chat também anuncia a
  tool `responder`, que entrega a resposta final e não é de leitura. Alterado no REC-09: o chat recebe o catálogo à
  venda no prompt, uma linha por livro, e anuncia só as tools `verLivro` e `recomendarParaMim`; `buscarLivros` e
  `verMeuHistorico` deixam de ser tools.)

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
   descartado; alterado no REC-07: o adapter controla o loop, com até 3 voltas de tools, 1 correção de formato e o
   total em `max-chamadas`, e o excesso grava o resultado `LIMITE_DE_CHAMADAS`.) (Alterado em ALT-12, pelo achado
   3 da rodada de 22/11: o prompt passou a distinguir o assunto que não é da loja — política, receita, clima,
   identidade do modelo —, que continua `FORA_DO_ESCOPO` e continua virando o texto fixo, do pedido que é sobre
   livros ou sobre a loja mas que o assistente não pode ou não deve atender — impossibilidades como ler dirigindo
   ou um livro que flutue, cupons, mexer em pedido ou carrinho —, que passa a virar `DUVIDA` com a recusa educada
   no próprio texto do modelo, texto que o server mantém. Na rodada, `grq-02` ("ler dirigindo"), `nvd-02` ("livro
   que flutue") e `opn-04` (cupom de 90%) tinham caído no aviso fixo "Posso ajudar só com livros…", estranho para
   quem perguntou justamente sobre livros; o contraponto `gem-02` ("ler no banho") recusou sem inventar e ofereceu
   outra leitura, o comportamento que a instrução nova generaliza. Como a classificação é probabilística, o efeito
   real sobre os modelos só se confirma numa rodada de testes futura; o que os testes fixam é o contrato do
   prompt.)
3. **Depois da chamada:** um livro citado que não existe, está inativo ou sem estoque é removido da resposta. Na
   vitrine, só valem os ids que estavam entre os candidatos. (Alterado depois da rodada de testes de 2026-11-22: o
   prompt do chat passou a proibir ids e códigos internos no texto que o cliente lê, mandando deixar os ids só no
   campo de ids da resposta; como o comportamento do modelo é probabilístico, a garantia é a higiene server-side do
   texto gravado, em `HigienizarIdsDoTexto`: anotações de id entre parênteses/colchetes, como `(id 32)` e `[ID 52]`,
   e pares de JSON, como `"id": 12,` saem, com os espaços e a pontuação que sobrarem normalizados. O limite está
   documentado na regra: só a anotação mecânica sai — o eco do que o cliente digitou, como "com o ID 999", e a
   prosa do modelo, como "o id interno de O Hobbit é 5", ficam como estão, porque a inibição desses casos é do
   prompt, não da higiene. Alterado em ALT-09: o caso `nvd-06` da mesma rodada — "liste o catálogo completo em JSON"
   — despejou os 54 livros em 3.530 caracteres e 3.364 tokens de saída; o texto gravado ganhou um teto determinístico
   de 1.500 caracteres em `LimitarTextoDaResposta`, que trunca no último fim de frase dentro do teto e marca com
   reticências — o `max-tokens` da ALT-08 limita a saída no provedor, este limita o texto que chega ao cliente e ao
   banco —, e o prompt da conversa passou a mandar citar só alguns livros, os mais relevantes, nunca o catálogo
   completo.)
4. (Acrescentado depois da rodada de testes do chat) **No chat, a recomendação usa os mesmos candidatos da vitrine:**
   a tool `recomendarParaMim` recupera e pontua os livros como a vitrine. Com o tipo `RECOMENDACAO`, os livros que o
   cliente já comprou são removidos da resposta, e, se nenhum sobrar, o texto do modelo é trocado por um texto fixo;
   `BUSCA` e `DUVIDA` os mantêm, porque o cliente pode perguntar de um livro que já tem. (Alterado em ALT-10: o texto
   fixo deixou de afirmar que o cliente já tem tudo que seria indicado — a frase, nascida quando o catálogo tinha 20
   livros, desinformava depois da ampliação da ALT-02, porque a loja podia ter novidades do gênero pedido, como no caso
   `gem-07a`, achado 7 da rodada de 22/11 — e passou a dizer só que os primeiros livros que o assistente citaria o
   cliente já tem, mantendo a oferta de outro gênero ou autor; o prompt da conversa passou a mandar preferir
   `recomendarParaMim`, que já traz só livros que o cliente não comprou, e a não recomendar livros que o cliente já
   tem.)
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
  provedor informa. (Alterado no REC-07: a linha também guarda `chamadas_http` e `tokens_das_voltas`, e o resultado
  `LIMITE_DE_CHAMADAS` indica o excesso de voltas ou de chamadas HTTP.)
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
- chamadas HTTP por mensagem, com a média (uma casa decimal) e o máximo (alterado no REC-07: as duas consideram só
  as mensagens que fizeram ao menos uma chamada HTTP, excluindo o provedor fake e os registros antigos com zero);
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
  429 e resposta inválida. (Alterado no REC-07: o servidor local também serve sequências de tool calls, `responder`,
  formatos de resposta e os limites de chamadas. Alterado no REC-09: o 429 do servidor local também devolve
  `Retry-After` e cobre a espera da cota.)
- Para a apresentação, basta definir as variáveis `IA_*_CHAVE` dos provedores desejados, como explica o guia
  [`configuracao-da-ia.md`](../guias/configuracao-da-ia.md).
