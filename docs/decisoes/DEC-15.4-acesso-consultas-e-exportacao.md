# DEC-15.4 — Ajustes de acesso, consultas e exportação

- **Status:** Aceita
- **Escopo:** Negócio
- **Data:** 2026-10-23
- **Card:** #72
- **Requisitos:** RF-CLI-003, RF-CLI-004, RN-CLI-007, RF-VEN-001, RF-ANA-004, RNF-GER-001

## Contexto

A revisão das decisões encontrou pontos soltos:

- a mensagem "Cadastro inativo." do [DEC-08](DEC-08-autenticacao-e-sessao.md) deixou de ter uso depois do
  [DEC-10](DEC-10-cliente-enderecos-e-cartoes.md);
- a tabela de autorização do DEC-08 não cobre os arquivos do `web` nem as rotas do profile `e2e`
  ([DEC-09](DEC-09-topologia-de-execucao.md));
- nada diz se o carrinho exige login;
- ordenar a consulta de clientes por ranking obriga a calcular o ranking de todos os filtrados
  ([DEC-04](DEC-04-ranking-de-cliente.md)), contra o limite de 1 segundo (RNF-GER-001);
- a exportação da análise estava como "CSV ou XLSX".

## Decisão

### Login de conta inativa

Não há resposta própria para conta inativa:

| Situação da conta ([DEC-10](DEC-10-cliente-enderecos-e-cartoes.md)) | Senha correta | Senha errada |
|---|---|---|
| `ATIVA` | login | 401 "Login ou senha inválidos." |
| `INATIVA` | reativa e faz login | 401 "Login ou senha inválidos." |
| `ENCERRADA` | 401 "Login ou senha inválidos." (identificador desconhecido) | 401 "Login ou senha inválidos." |

### Rotas públicas

Somam-se às rotas públicas do DEC-08:

- os arquivos estáticos e as rotas do `web` servidos pelo Spring (`GET` fora de `/api` e `/v3`, DEC-09);
- `/api/teste/**`, que só existe no profile `e2e`, sem exigir o token CSRF.

### Carrinho

O carrinho exige login com papel `CLIENTE` (`/api/me/carrinho`). Visitante só navega na vitrine; ao adicionar um
livro, o `web` o leva ao login.

### Consulta de clientes

A consulta de clientes do admin (RF-CLI-004) não ordena por ranking. O ranking só é exibido, calculado para os
clientes da página.

### Exportação da análise (RF-ANA-004)

- Só **CSV**, gerado pelo `server` em `GET /api/admin/analise/exportacao`, com os mesmos filtros da consulta.
- Colunas `mes` (`AAAA-MM`), `categoria` e `valor`, uma linha por mês e categoria, incluindo os meses com R$ 0,00
  (RN-ANA-003).
- Formatado para abrir direto no Excel em português: UTF-8 com BOM, separador `;` e valor com vírgula decimal e sem
  separador de milhar (`1234,56`).

## Alternativas consideradas

- Mensagem própria para conta inativa: só apareceria com a senha errada, e diria que o identificador existe.
- Carrinho anônimo, juntado ao do cliente no login: as reservas de estoque ficariam sem dono até o login.
- Ordenar por ranking com o ranking gravado: exigiria recalcular a recência todo dia.
- XLSX: exige biblioteca nova ou OOXML escrito à mão para o mesmo conteúdo.
- CSV gerado no `web` com os dados do gráfico: a regra de formato ficaria fora do `server`.

## Consequências

- A configuração do Spring Security do INF-06 inclui as rotas do `web` e o `/api/teste/**` (no profile `e2e`).
- O ranking não está entre os campos ordenáveis da consulta de clientes.
- O ANA-04 tem um endpoint que devolve `text/csv` com `Content-Disposition: attachment`.
