# DEC-10 — Modelo de cliente, endereços e cartões

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-22
- **Card:** #63
- **Requisitos:** RF-CLI-001, RF-CLI-003, RF-CLI-004, RF-CLI-006, RF-CLI-007, RNF-CLI-005, RN-CLI-001 a RN-CLI-006,
  RF-VEN-005, RF-VEN-006

## Contexto

O DRS lista os dados do cliente, do endereço e do cartão, mas deixa em aberto: os papéis do endereço (residencial,
cobrança, entrega), quais endereços têm "nome curto", o que se guarda do cartão, a regra do cartão preferencial, a
exclusão de endereços e cartões já usados em pedidos, o formato do código de cliente, a origem dos valores de domínio
e os efeitos da inativação (RF-CLI-003).

## Decisão

### Endereço

- O endereço **não tem papel**. Se ele é de entrega ou de cobrança, o cliente escolhe a cada compra (RF-VEN-005).
- Campos: nome curto, tipo de endereço, tipo de logradouro, logradouro, número, bairro, CEP, cidade, estado e país
  (obrigatórios); observações (opcional).
- **Nome curto** (RF-CLI-006) obrigatório em todo endereço ("Casa", "Trabalho da Ana").
- **Tipo de endereço:** `CASA` ou `TRABALHO`. É o "tipo de residência" do RN-CLI-003.
- **Só Brasil:** o país é sempre "Brasil" (gravado, sem campo editável na UI); estado é uma das 27 UFs; CEP com 8
  dígitos. É o que o CPF (RN-CLI-006) e o frete por CEP ([DEC-05.3](DEC-05.3-frete.md)) já pressupõem.
- O cliente tem **ao menos um endereço**, de qualquer tipo, desde o cadastro. Esse mínimo atende RN-CLI-001,
  RN-CLI-002 e o endereço residencial do RN-CLI-006. Não se exclui o último endereço.

### Endereços no pedido

- O checkout pede o endereço de **entrega** e o de **cobrança**, ambos entre os do cliente ou um novo (RF-VEN-005); a
  cobrança vem preenchida igual à entrega.
- O pedido guarda uma **cópia** dos dois endereços e, de cada cartão, bandeira, 4 últimos dígitos e valor pago. Por
  isso alterar ou excluir um endereço ou cartão do cadastro não muda pedidos antigos.

### Cartão de crédito

- Campos: número, nome impresso, bandeira e validade ([DEC-06](DEC-06-validade-do-cartao.md)).
- **O código de segurança não é gravado.** O checkout pede o CVV de cada cartão usado, inclusive dos salvos, e o
  repassa à operadora fake ([DEC-05.2](DEC-05.2-operadora-de-cartao.md)) sem guardá-lo, nem no pedido. Um cartão novo
  incorporado ao perfil no checkout (RF-VEN-006) também é salvo sem ele.
- A API devolve o número mascarado (`**** **** **** 1234`), nunca o número completo.
- Bandeira em tabela do seed (RN-CLI-005).
- Cartão é opcional no cadastro. **Havendo ao menos um, exatamente um é preferencial** (RF-CLI-007):
  - o primeiro cartão cadastrado vira preferencial;
  - marcar outro como preferencial desmarca o anterior;
  - excluir o preferencial promove o cartão restante cadastrado mais recentemente.

### Exclusão

Endereços e cartões são excluídos fisicamente, já que o pedido tem a própria cópia. O cliente nunca é excluído: o
fim da conta é a inativação.

### Código de cliente (RNF-CLI-005)

O código único é o próprio **id** do cliente, sem campo extra. É o valor exibido ao usuário e usado como filtro na
consulta do admin (igualdade, [DEC-07](DEC-07-convencoes-da-api-rest.md)) (alterado pelo
[DEC-14](DEC-14-ids-nomes-e-seed.md): o código é exibido, mas não é filtro da consulta do admin).

### Valores de domínio

Enums no código:

| Enum | Valores |
|---|---|
| `Genero` | `FEMININO`, `MASCULINO`, `OUTRO`, `NAO_INFORMADO` |
| `TipoTelefone` | `CELULAR`, `FIXO` |
| `TipoEndereco` | `CASA`, `TRABALHO` |
| `TipoLogradouro` | `RUA`, `AVENIDA`, `ALAMEDA`, `TRAVESSA`, `PRACA`, `ESTRADA`, `RODOVIA`, `OUTRO` |
| `Uf` | as 27 siglas |

### Telefone e login

- O cliente tem um telefone (tipo, DDD e número, RN-CLI-006), **único** entre as contas em uso pela combinação DDD +
  número, sem considerar o tipo.
- Telefone e e-mail ficam no `Usuario` ([DEC-08](DEC-08-autenticacao-e-sessao.md)), e os dois servem de login. O
  administrador não tem telefone.
- `POST /api/sessao` recebe `{ login, senha }`: com `@`, o login é o e-mail; sem, é o telefone, comparado só pelos
  dígitos (DDD + número).
- Login inválido responde 401 com "Login ou senha inválidos.".

### Inativação e encerramento (RF-CLI-003)

- **Só o próprio cliente** inativa a conta (`POST /api/me/inativar`; na UI, "excluir minha conta"). O admin não
  inativa nem reativa.
- Ao inativar: a sessão é encerrada, o carrinho é esvaziado e as reservas de estoque, liberadas. Pedidos e trocas em
  andamento seguem o fluxo normal no admin.
- **Reativação:** login com a senha correta em até **7 dias** após a inativação reativa a conta.
- Depois de 7 dias, a conta fica **encerrada**: não é reativável, e CPF, e-mail e telefone ficam livres para um novo
  cadastro, que gera outro cliente (outro id, ranking do zero). A conta encerrada continua no histórico, nas análises
  e na auditoria.

Situação da conta, calculada sobre o `Usuario` (`situacao` ATIVO/INATIVO e `inativadoEm`) e o `Relogio`:

| Situação | Condição |
|---|---|
| `ATIVA` | situação ATIVO |
| `INATIVA` | situação INATIVO e `inativadoEm` há no máximo 7 dias |
| `ENCERRADA` | situação INATIVO e `inativadoEm` há mais de 7 dias |

Nada é gravado quando o prazo vence: a situação é calculada na consulta. A regra fica **centralizada no `acesso`**,
e nenhum outro módulo conhece o prazo:

- a regra de domínio `SituacaoDaConta` calcula a situação de um usuário;
- a porta de usuários do `acesso` só encontra **contas em uso** (`ATIVA` ou `INATIVA`); a condição de tempo é
  escrita uma vez, no adapter dela;
- login e unicidade de e-mail e telefone usam essa porta. Login com identificador de conta encerrada responde como
  identificador desconhecido;
- a unicidade do CPF, que fica no `Cliente`, é checada por `CpfUnico` (módulo `clientes`): busca os clientes com o
  CPF e pergunta ao `acesso` se a conta de algum está em uso;
- a consulta de clientes do admin (RF-CLI-004) filtra por situação pedindo ao `acesso` os ids dos usuários na
  situação escolhida.

No resto vale o DEC-08: a cada requisição autenticada, usuário que não esteja `ATIVA` recebe 401.

## Alternativas consideradas

- Endereço com papéis fixos (residencial, cobrança, entrega): obriga a cadastrar o mesmo endereço mais de uma vez ou
  a manter flags que a compra já resolve.
- Tipo de residência (casa, apartamento) separado do tipo de endereço: dois campos quase iguais.
- Endereços internacionais: sem CPF nem frete calculável fora do Brasil.
- Gravar o CVV: o DRS o lista no RN-CLI-004, mas pedir a cada compra é mais próximo do real.
- Bloquear a exclusão do cartão preferencial: obriga um passo a mais sem ganho.
- Exclusão lógica de endereço e cartão: o pedido ainda mudaria se o endereço fosse alterado.
- Código de cliente sequencial (`CLI-000001`) ou aleatório: um segundo identificador sem necessidade.
- Tabelas do seed para todos os domínios: entidades, repositórios e endpoints de lista para valores fixos.
- Admin inativa e reativa: o DRS não pede, e a inativação pelo cliente com prazo de reativação basta.
- Reativação sem prazo: CPF e e-mail nunca seriam liberados.
- Job que grava `ENCERRADA`: a situação ficaria defasada entre execuções.

## Consequências

- Desvios do DRS, registrados nas matrizes: RN-CLI-001, RN-CLI-002 e RN-CLI-006 reduzidos a "ao menos um
  endereço"; o tipo de residência do RN-CLI-003 vira `CASA`/`TRABALHO`; o RN-CLI-004 sem CVV gravado; a inativação
  (RF-CLI-003) feita só pelo cliente.
- A unicidade de CPF, e-mail e telefone é checada pela aplicação, sem `UNIQUE` no banco, porque depende do tempo.
  Dois cadastros simultâneos com o mesmo dado podem passar; o risco é aceito.
- Uma conta encerrada perde o acesso aos cupons de troca e às trocas ainda abertas.
- O seed não tem CVV e tem telefones distintos por cliente.
