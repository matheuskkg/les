# Cadastro de Clientes

Prefixo do grupo: `CLI`. Fonte: DRS – E-commerce de Livros (LES 2026), v0.6.

## Requisitos funcionais

| ID | Nome | Descrição |
|---|---|---|
| RF-CLI-001 | Cadastrar cliente | O sistema deve possibilitar o cadastro de clientes. |
| RF-CLI-002 | Alterar cliente | O sistema deve possibilitar a alteração de dados cadastrais de clientes. |
| RF-CLI-003 | Inativar cadastro de cliente | O sistema deve possibilitar que clientes sejam inativados. |
| RF-CLI-004 | Consulta de clientes | O sistema deve possibilitar que um cliente seja consultado com base em um filtro definido pelo usuário. Todos os campos utilizados para identificação do cliente podem ser utilizados como filtro, de forma combinada ou isolada. |
| RF-CLI-005 | Consulta de transações | O sistema deve disponibilizar no cadastro de clientes a consulta de todas as transações já realizadas pelo mesmo. |
| RF-CLI-006 | Cadastro de endereços de entrega | Deve ser possível associar diversos endereços de entrega ao cadastro de um cliente. Cada endereço deve ser identificado com um nome composto de uma frase curta. |
| RF-CLI-007 | Cadastro de cartões de crédito | Deve ser possível associar diversos cartões de crédito ao cadastro de um cliente. Deve haver um cartão de crédito configurado como preferencial. |
| RF-CLI-008 | Alteração apenas de senha | O sistema deve possibilitar que a senha do usuário seja alterada sem que seja necessária a alteração de todos os dados cadastrais. |

## Requisitos não funcionais

| ID | Nome | Descrição |
|---|---|---|
| RNF-CLI-001 | Senha forte | A senha cadastrada pelo usuário deve ser composta de pelo menos 8 caracteres, ter letras maiúsculas e minúsculas além de conter caracteres especiais. |
| RNF-CLI-002 | Confirmação de senha | O usuário obrigatoriamente deve digitar duas vezes a mesma senha no momento do registro da mesma. |
| RNF-CLI-003 | Senha criptografada | A senha deve ser criptografada. |
| RNF-CLI-004 | Alteração apenas de endereços | O sistema deve possibilitar que endereços de entrega ou cobrança possam ser alterados ou adicionados de forma simples sem a necessidade da edição dos demais dados cadastrais. |
| RNF-CLI-005 | Código de cliente | Todo cliente cadastrado deve receber um código único no sistema. |

## Regras de negócio

| ID | Nome | Descrição |
|---|---|---|
| RN-CLI-001 | Cadastro de endereço de cobrança | Para todo cliente cadastrado é obrigatório o registro de ao menos um endereço de cobrança. |
| RN-CLI-002 | Cadastro de endereço de entrega | Para todo cliente cadastrado é obrigatório o registro de ao menos um endereço de entrega. |
| RN-CLI-003 | Composição do registro de endereços | Todo endereço deve conter: tipo de residência, tipo de logradouro, logradouro, número, bairro, CEP, cidade, estado e país (obrigatórios); opcionalmente, observações. |
| RN-CLI-004 | Composição do registro de cartões de crédito | Todo cartão de crédito deverá ser composto por: nº do cartão, nome impresso, bandeira e código de segurança. |
| RN-CLI-005 | Bandeiras permitidas | Todo cartão de crédito associado a um cliente deverá ser de alguma bandeira registrada no sistema. |
| RN-CLI-006 | Dados obrigatórios para o cadastro de um cliente | Obrigatórios: gênero, nome, data de nascimento, CPF, telefone (tipo, DDD e número), e-mail, senha, endereço residencial. |
| RN-CLI-007 | Ranking de cliente | O cliente deve receber um ranking numérico com base no seu perfil de compra. |

## Matriz de rastreabilidade

Status: ✅ implementado · 🟡 parcial · ❌ pendente.

| ID | Status | Onde no código | Observação |
|---|---|---|---|
| RF-CLI-001 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/clientes/aplicacao/casodeuso/CadastrarCliente.java`, `apps/server/src/main/java/br/fatec/les/livraria/clientes/infraestrutura/web/ClienteController.java`, `apps/web/src/paginas/Cadastro.tsx` | Cadastro com ao menos um endereço; CPF, e-mail e telefone únicos entre as contas em uso ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). |
| RF-CLI-002 | ❌ |  |  |
| RF-CLI-003 | 🟡 | `apps/server/src/main/java/br/fatec/les/livraria/acesso/dominio/modelo/SituacaoDaConta.java`, `apps/server/src/main/java/br/fatec/les/livraria/acesso/aplicacao/casodeuso/Autenticar.java` | Só o próprio cliente inativa; login em até 7 dias reativa; depois a conta fica encerrada e libera CPF, e-mail e telefone. A situação fica no `Usuario` e é calculada no `acesso` ([DEC-08](../decisoes/DEC-08-autenticacao-e-sessao.md), [DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). Desvio: o DRS não diz quem inativa. |
| RF-CLI-004 | ❌ |  | Filtros por query params combinados com E, via `Specification` no adapter ([DEC-07](../decisoes/DEC-07-convencoes-da-api-rest.md)). O filtro de situação (`ATIVA`, `INATIVA`, `ENCERRADA`) vem do `acesso` ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). Sem ordenação por ranking ([DEC-15.4](../decisoes/DEC-15.4-acesso-consultas-e-exportacao.md)). |
| RF-CLI-005 | ❌ |  |  |
| RF-CLI-006 | ❌ |  | Endereço sem papel fixo: entrega e cobrança são escolhidas na compra. Nome curto obrigatório em todo endereço ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). |
| RF-CLI-007 | ❌ |  | Cartão opcional; havendo algum, exatamente um preferencial; excluir o preferencial promove o mais recente ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). |
| RF-CLI-008 | ❌ |  | Caso de uso do módulo `acesso`; mantém a sessão atual ([DEC-08](../decisoes/DEC-08-autenticacao-e-sessao.md)). |
| RNF-CLI-001 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/clientes/dominio/regra/SenhaForte.java` |  |
| RNF-CLI-002 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/clientes/dominio/regra/ConfirmacaoDeSenha.java` |  |
| RNF-CLI-003 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/acesso/aplicacao/porta/CodificadorDeSenha.java`, `apps/server/src/main/java/br/fatec/les/livraria/acesso/infraestrutura/seguranca/CodificadorDeSenhaBCrypt.java` | BCrypt pela porta `CodificadorDeSenha` ([DEC-08](../decisoes/DEC-08-autenticacao-e-sessao.md)). |
| RNF-CLI-004 | ❌ |  |  |
| RNF-CLI-005 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/clientes/infraestrutura/persistencia/ClienteJpa.java` | O código é o id do cliente ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). Exibido, mas não é filtro da consulta do admin; ID `Long` por sequence ([DEC-14](../decisoes/DEC-14-ids-nomes-e-seed.md)). |
| RN-CLI-001 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/clientes/dominio/regra/EnderecoDoCliente.java` | Desvio: sem endereço de cobrança fixo; basta ao menos um endereço, e a cobrança é escolhida na compra ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). |
| RN-CLI-002 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/clientes/dominio/regra/EnderecoDoCliente.java` | Desvio: sem endereço de entrega fixo; basta ao menos um endereço, e a entrega é escolhida na compra ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). |
| RN-CLI-003 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/clientes/dominio/regra/EnderecoCompleto.java` | Desvio: o tipo de residência é o tipo de endereço `CASA`/`TRABALHO`. Só Brasil: país fixo, estado por UF, CEP com 8 dígitos ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). |
| RN-CLI-004 | 🟡 | `apps/server/src/main/java/br/fatec/les/livraria/clientes/dominio/regra/CartaoCompleto.java`, `apps/server/src/main/java/br/fatec/les/livraria/clientes/aplicacao/regra/ValidadeDoCartao.java` | Estendido por [DEC-06](../decisoes/DEC-06-validade-do-cartao.md): o cartão também tem data de validade (mês/ano), obrigatória e não vencida no cadastro. Desvio: o código de segurança não é gravado, é pedido a cada compra ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). |
| RN-CLI-005 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/clientes/aplicacao/regra/BandeiraRegistrada.java`, `apps/server/src/main/resources/seed/200_bandeiras.sql` | Bandeiras em tabela do seed ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)), no módulo `clientes`, com seed `2NN` ([DEC-16](../decisoes/DEC-16-modelo-de-dados.md)). |
| RN-CLI-006 | ✅ | `apps/server/src/main/java/br/fatec/les/livraria/clientes/dominio/regra/DadosObrigatoriosCliente.java`, `apps/server/src/main/java/br/fatec/les/livraria/clientes/dominio/regra/TamanhosMaximosCliente.java`, `apps/server/src/main/java/br/fatec/les/livraria/clientes/aplicacao/regra/CpfUnico.java`, `apps/server/src/main/java/br/fatec/les/livraria/clientes/aplicacao/regra/EmailUnico.java`, `apps/server/src/main/java/br/fatec/les/livraria/clientes/aplicacao/regra/TelefoneUnico.java`, `apps/server/src/main/java/br/fatec/les/livraria/clientes/aplicacao/regra/DataNascimentoValida.java` | Desvio: o endereço residencial vira "ao menos um endereço" (`CASA` ou `TRABALHO`). Telefone único, também usado no login ([DEC-10](../decisoes/DEC-10-cliente-enderecos-e-cartoes.md)). |
| RN-CLI-007 | ❌ |  | Fórmula definida em [DEC-04](../decisoes/DEC-04-ranking-de-cliente.md): RFM 0–100 (valor 50%, frequência 30%, recência 20%) sobre pedidos dos últimos 12 meses, calculado sob demanda. Considera pedidos `APROVADA`, `EM TRANSPORTE` e `ENTREGUE` e desconta os cupons gerados por trocas concluídas ([DEC-12](../decisoes/DEC-12-pedido-checkout-e-trocas.md)). Arredondado com `HALF_UP` ([DEC-15.1](../decisoes/DEC-15.1-arredondamento.md)). |
