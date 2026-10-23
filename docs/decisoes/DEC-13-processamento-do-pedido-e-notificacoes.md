# DEC-13 — Processamento do pedido e notificações

- **Status:** Aceita
- **Escopo:** Tecnologia
- **Data:** 2026-10-23
- **Card:** #66
- **Requisitos:** RF-VEN-008, RN-VEN-002, RN-VEN-007, RN-VEN-008, RN-VEN-014, RN-VEN-016, RN-EST-006, RF-EST-003,
  RNF-GER-002

## Contexto

A finalização é síncrona: a operadora fake é chamada na própria requisição
([DEC-11](DEC-11-cupons-e-distribuicao-do-pagamento.md)). Falta definir:

- como os módulos disparam os efeitos uns nos outros: estoque, cupons, reservas e notificações;
- o que é desfeito quando o pagamento é reprovado;
- como tratar o duplo clique em "Finalizar compra";
- quais eventos notificam o usuário e como a notificação chega ao `web`.

## Decisão

### Comunicação entre módulos

**Chamada direta** ao caso de uso ou à porta pública do outro módulo, dentro da transação de quem chama, como já
diz o `arquitetura.md`. Não há eventos de domínio (`ApplicationEvent`). A única ação depois do commit é o envio por
SSE (abaixo), que fica na infraestrutura.

### Finalização em duas transações

`FinalizarCompra` não é transacional e orquestra:

1. **`RegistrarPedido`** (transação 1):
   - valida o checkout (aberto, sem pedido `EM PROCESSAMENTO`), o estoque reservado (RN-VEN-002) e as regras de
     pagamento ([DEC-11](DEC-11-cupons-e-distribuicao-do-pagamento.md));
   - cria o pedido `EM PROCESSAMENTO` com as cópias ([DEC-12](DEC-12-pedido-checkout-e-trocas.md));
   - marca os cupons como usados.

   Ao fim, faz o commit.
2. **Operadora fake**, fora de transação, com o número e o CVV em memória.
3. **`AprovarPedido`** ou **`ReprovarPedido`** (transação 2):

   | Desfecho | Efeitos |
   |---|---|
   | `APROVADA` | baixa do estoque, consumindo as reservas com lock pessimista (RF-EST-003); cupom de troco; checkout concluído; notificação |
   | `REPROVADA` | cupons devolvidos e notificação; o checkout e as reservas continuam |

A resposta da requisição traz o pedido já no status final. Com o commit intermediário, a auditoria (RNF-GER-002)
registra as duas transições.

Se a transação 2 falhar por erro técnico, a requisição responde 500 e o pedido fica `EM PROCESSAMENTO`. Ao expirar o
checkout, a tarefa de expiração reprova esse pedido e devolve os cupons, já que o número e o CVV não existem mais.

### Duplo clique

Não há chave de idempotência:

- O `web` desabilita o botão enquanto envia.
- No servidor, a segunda finalização encontra o checkout com pedido `EM PROCESSAMENTO` e responde 409. O checkout
  tem `@Version`, então duas finalizações simultâneas também resultam em 409 para a segunda.
- Os cupons têm `@Version` próprio ([DEC-11](DEC-11-cupons-e-distribuicao-do-pagamento.md)).

### Expiração

A tarefa `ExpirarReservas` (`TarefaPorTempo`, [DEC-09](DEC-09-topologia-de-execucao.md)) roda a cada 30 s. Para o
carrinho e para o checkout, ela:

- gera o aviso de expiração uma vez, quando faltar a antecedência configurada ou menos (RN-VEN-014):
  `livraria.carrinho.aviso-expiracao` no carrinho e `livraria.checkout.aviso-expiracao` no checkout;
- no vencimento, libera as reservas, move os itens para os itens removidos do carrinho (RN-VEN-015, RNF-VEN-001) e
  notifica.

O `web` não calcula prazos. O `GET` do carrinho e do checkout devolve `expiraEm` só para exibição.

### Notificações

Ficam num módulo próprio, **`notificacoes`**, genérico: ele não conhece pedidos, trocas nem carrinhos.

- A notificação é persistida por usuário (`Usuario` do [DEC-08](DEC-08-autenticacao-e-sessao.md)) e tem:
  - `tipo` (texto definido pelo módulo de origem);
  - `mensagem`;
  - referência opcional (`recurso` e `id`, como `pedido` e `10042`), que o `web` usa para montar o link;
  - `criadaEm` e `lidaEm`.
- Os outros módulos notificam pelos casos de uso públicos do módulo, chamados diretamente no caso de uso que causa
  o evento:
  - `Notificar`, para um usuário;
  - `NotificarAdministradores`, que pede ao `acesso` os usuários `ADMIN`.
- O stream SSE, as rotas e a entrega depois do commit também ficam em `notificacoes`.

Tipos gerados pelo `vendas`:

| Destinatário | Tipo | Evento |
|---|---|---|
| Cliente | `CARRINHO_EXPIRANDO`, `CHECKOUT_EXPIRANDO` | aviso de expiração (RN-VEN-014) |
| Cliente | `CARRINHO_EXPIRADO`, `CHECKOUT_EXPIRADO` | itens removidos por expiração (RN-VEN-014, RN-VEN-002) |
| Cliente | `PEDIDO_APROVADO`, `PEDIDO_REPROVADO` | resultado do pagamento |
| Cliente | `PEDIDO_DESPACHADO`, `PEDIDO_ENTREGUE` | despacho e entrega |
| Cliente | `TROCA_AUTORIZADA` | troca autorizada (RN-VEN-016) |
| Cliente | `TROCA_RECUSADA` | troca recusada, com o motivo |
| Cliente | `TROCA_CONCLUIDA` | recebimento confirmado e cupom gerado |
| Admin | `TROCA_SOLICITADA` | nova troca para autorizar |
| Admin | `PEDIDO_AGUARDANDO_DESPACHO` | pedido aprovado |

As notificações de admin são criadas para cada usuário `ADMIN`.

**Rotas** (`/api/me` para o cliente, `/api/admin` com as mesmas rotas para o admin):

| Rota | Uso |
|---|---|
| `GET /api/me/notificacoes` | histórico paginado, não lidas primeiro e mais recentes antes |
| `GET /api/me/notificacoes/nao-lidas` | `{ quantidade }`, para o contador do cabeçalho |
| `POST /api/me/notificacoes/{id}/ler` | marca uma como lida |
| `POST /api/me/notificacoes/ler-todas` | marca todas como lidas |
| `GET /api/me/eventos` | stream SSE (`text/event-stream`) |

### Envio por SSE

- `SseEmitter` do Spring MVC, sem dependência nova. O `web` usa o `EventSource` nativo. Autenticação pelo cookie da
  sessão: a origem é a mesma, e em dev o proxy do Vite repassa a requisição ([DEC-09](DEC-09-topologia-de-execucao.md)).
- A notificação persistida é a fonte da verdade. O stream só a entrega, **depois do commit**
  (`TransactionSynchronization.afterCommit`), como evento `notificacao`:
  - `id` é o id da notificação;
  - `data` é o mesmo DTO da listagem.
- Na reconexão, o `EventSource` envia `Last-Event-ID`, e o servidor reenvia as notificações do usuário com id
  maior.
- Só passam eventos `notificacao`. O `web` recarrega a tela afetada (carrinho, checkout, pedido, troca) conforme o
  tipo.
- Os emissores ficam em memória por usuário: uma instância só ([DEC-09](DEC-09-topologia-de-execucao.md)).
  - Comentário de heartbeat a cada 25 s.
  - Timeout de 30 min; o `EventSource` reconecta sozinho.
  - Os emissores são removidos ao completar, dar erro ou expirar, e fechados no logout.
- Com a sessão vencida, a reconexão recebe 401. O `EventSource` para, e o `web` trata como sessão encerrada.

## Alternativas consideradas

- Eventos de domínio do Spring (`@TransactionalEventListener`): desacoplam, mas espalham o fluxo e a ordem dos
  efeitos e contrariam a comunicação por casos de uso do `arquitetura.md`.
- Finalização numa transação só: o log de transação nunca mostraria `EM PROCESSAMENTO`.
- Header `Idempotency-Key`: guardar e repetir respostas para um caso que o 409 já cobre.
- Polling das notificações: atraso de até um intervalo e requisições vazias constantes.
- WebSocket: bidirecional sem necessidade e com dependência extra.
- Notificar só a troca autorizada (o mínimo do DRS): o cliente não saberia da reprovação nem da expiração.
- Notificações dentro de `vendas`: o canal de entrega, que é genérico, ficaria preso a um módulo de negócio.
- Prazos de expiração calculados no `web`: duplica a regra e diverge do servidor.
- Eventos de estado sem notificação no stream: dois tipos de mensagem para o mesmo efeito na tela.

## Consequências

- `FinalizarCompra` orquestra três passos, e o teste do caso de uso cobre aprovação, reprovação e falha entre as
  transações.
- O estoque expõe a baixa e a liberação de reservas como casos de uso chamados por `vendas`.
- O cliente OpenAPI não tipa o stream, mas o DTO do evento é o mesmo da listagem, que está no contrato.
- O E2E testa as notificações com o relógio controlado (`POST /api/teste/relogio`), que executa a tarefa de
  expiração.
