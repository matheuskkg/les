# Plano de Testes

![](imagens/exemplo-plano-de-testes-sga-01.png)

**Sistema de Gestão Acadêmica (SGA)**

**Sprint 3: Cadastro, Matrícula, Notas e Histórico**

**Histórico de Versões**

| **Data** | **Versão** | **Descrição** | **Autor** | **Revisor** |
| :-: | :-: | :-: | :-: | :-: |
| 05/10/26 | 1.0 | Versão inicial: escopo, estratégia e casos de teste da Sprint 3 | Ana Beatriz Lima | Carlos Eduardo Nunes |
| 12/10/26 | 1.1 | Inclusão dos resultados de execução e do registro de defeitos | Lucas Martins | Fernanda Oliveira |
|  |  |  |  |  |
|  |  |  |  |  |

| **Cliente** | Secretaria Acadêmica (cliente simulado) |
| -: | - |
| **Documento** | Plano de Testes: Sistema de Gestão Acadêmica (SGA) |
| **Data** | 12 de outubro de 2026 |
| **Autor(es)** | **Ana Beatriz Lima **RA 0000000001 **Carlos Eduardo Nunes **RA 0000000002 **Fernanda Oliveira **RA 0000000003 **Lucas Martins** RA 0000000004 |
| **Documento base** | DRS\_SGA versão 1.2 |

**Página de Assinaturas**

| Revisado e Aprovado por: | Carlos Eduardo Nunes (líder do grupo) |
| - | - |
|  | 12.10.26 |
| Revisado e Aprovado por: | Professor responsável |
|  | dd.mm.aa |

**Índice**

- [1. Introdução](#1-introdução)
- [1.1 Objetivo](#11-objetivo)
- [1.2 Escopo](#12-escopo)
- [1.3 Documentos de referência](#13-documentos-de-referência)
- [1.4 Definições e siglas](#14-definições-e-siglas)
- [2. Itens a serem testados](#2-itens-a-serem-testados)
- [3. Itens fora do escopo](#3-itens-fora-do-escopo)
- [4. Estratégia de testes](#4-estratégia-de-testes)
- [4.1 Níveis de teste](#41-níveis-de-teste)
- [4.2 Tipos de teste](#42-tipos-de-teste)
- [4.3 Técnicas de projeto de casos de teste](#43-técnicas-de-projeto-de-casos-de-teste)
- [5. Ambiente de testes](#5-ambiente-de-testes)
- [6. Critérios](#6-critérios)
- [7. Casos de teste](#7-casos-de-teste)
- [7.1 Resumo dos casos de teste](#71-resumo-dos-casos-de-teste)
- [7.2 Especificação dos casos de teste](#72-especificação-dos-casos-de-teste)
- [8. Matriz de rastreabilidade](#8-matriz-de-rastreabilidade)
- [9. Cronograma e responsabilidades](#9-cronograma-e-responsabilidades)
- [10. Riscos e contingências](#10-riscos-e-contingências)
- [11. Gestão de defeitos](#11-gestão-de-defeitos)
- [11.1 Classificação de severidade](#111-classificação-de-severidade)
- [11.2 Registro de defeitos](#112-registro-de-defeitos)
- [12. Entregáveis](#12-entregáveis)

*Para atualizar o índice no Word, clique com o botão direito sobre ele e escolha "Atualizar campo".*

## 1. Introdução

### 1.1 Objetivo

Este documento define o planejamento dos testes do incremento entregue na Sprint 3 do Sistema de Gestão Acadêmica (SGA), incluindo o cadastro de alunos, a matrícula em disciplinas, o lançamento de notas e a emissão do histórico escolar. O plano estabelece o que será testado, as técnicas e ferramentas a serem utilizadas, os responsáveis e os critérios que determinam se o incremento está apto a ser apresentado ao cliente na revisão da sprint.

### 1.2 Escopo

Estão cobertos os requisitos RF001 a RF004, as regras de negócio RN001 a RN008 e os requisitos não funcionais RNF001 a RNF003 do DRS\_SGA versão 1.2. Os testes abrangem os níveis unitário, de integração e de sistema, com automação dos casos de maior prioridade.

### 1.3 Documentos de referência

| **Documento** | **Versão** | **Observação** |
| :-: | :-: | :-: |
| DRS\_SGA: Documento de Requisitos de Software | 1.2 | Fonte de todos os requisitos rastreados |
| Especificações de Caso de Uso UC01 a UC04 | 1.0 | Fluxos principais e de exceção utilizados nos casos de teste |
| DVP\_SGA: Documento de Visão do Projeto | 1.0 | Contexto e objetivos do sistema |

### 1.4 Definições e siglas

| **Termo** | **Definição** |
| :-: | :-: |
| RF / RN / RNF | Requisito Funcional / Regra de Negócio / Requisito Não Funcional |
| CT | Caso de Teste |
| E2E | End to End: teste que percorre o fluxo completo pela interface do usuário |
| RA | Registro Acadêmico: identificador único do aluno |
| Período de matrícula | Intervalo de datas definido pela secretaria em que a matrícula é permitida |

## 2. Itens a serem testados

| **Funcionalidade** | **Requisitos (DRS)** | **Caso de uso** | **Prioridade** |
| :-: | :-: | :-: | :-: |
| Cadastro de alunos | RF001, RN001, RN002 | UC01 | Alta |
| Matrícula em disciplinas | RF002, RN003, RN004, RN005, RN008 | UC02 | Alta |
| Lançamento de notas e frequência | RF003, RN006, RN007 | UC03 | Alta |
| Emissão de histórico escolar | RF004, RNF001 | UC04 | Média |
| Requisitos transversais | RNF002, RNF003 | Todos | Média |

**Requisitos do DRS\_SGA considerados neste plano**

| **ID** | **Descrição** |
| :-: | :-: |
| RF001 | O sistema deve permitir cadastrar, alterar, consultar e inativar alunos |
| RF002 | O sistema deve permitir ao aluno realizar matrícula nas disciplinas oferecidas no semestre |
| RF003 | O sistema deve permitir ao professor lançar notas e frequência dos alunos de suas turmas |
| RF004 | O sistema deve permitir ao aluno emitir o histórico escolar em PDF |
| RN001 | O CPF do aluno deve ser válido e único no sistema |
| RN002 | O RA deve ser gerado automaticamente pelo sistema no formato AAAASNNNN |
| RN003 | O aluno pode estar matriculado em no máximo 7 disciplinas por semestre |
| RN004 | A matrícula só é permitida se o aluno tiver sido aprovado nas disciplinas pré-requisito |
| RN005 | Cada turma possui capacidade máxima de 40 alunos |
| RN006 | A nota deve estar entre 0,0 e 10,0, com no máximo uma casa decimal |
| RN007 | É aprovado o aluno com média final maior ou igual a 6,0 e frequência maior ou igual a 75% |
| RN008 | A matrícula só pode ser realizada dentro do período de matrícula definido pela secretaria |
| RNF001 | A emissão do histórico escolar deve ocorrer em no máximo 3 segundos |
| RNF002 | As senhas dos usuários devem ser armazenadas com hash criptográfico |
| RNF003 | O sistema deve funcionar nas versões atuais dos navegadores Chrome e Firefox |

## 3. Itens fora do escopo

| **Item** | **Motivo da exclusão** |
| :-: | :-: |
| Trancamento e cancelamento de matrícula | Funcionalidade prevista para a Sprint 4 |
| Emissão de diploma e certificados | Não faz parte do DRS\_SGA versão 1.2 |
| Integração com o sistema financeiro | Não faz parte do DRS\_SGA versão 1.2 |
| Testes de carga com múltiplos usuários simultâneos | Não há RNF que defina volume de usuários; registrado como risco R3 |

## 4. Estratégia de testes

### 4.1 Níveis de teste

| **Nível** | **Objetivo** | **Ferramenta** | **Responsável** |
| :-: | :-: | :-: | :-: |
| Unitário | Verificar isoladamente as validações de CPF e nota e o cálculo da situação final | JUnit 5 | Lucas Martins |
| Integração | Verificar as regras de matrícula envolvendo serviço e banco de dados | JUnit 5 com H2 em memória | Lucas Martins |
| Sistema (E2E) | Verificar os fluxos completos de cadastro, matrícula e histórico pela interface | Cypress 13 | Ana Beatriz Lima e Fernanda Oliveira |
| Aceitação | Demonstrar os fluxos ao cliente na revisão da sprint | Execução manual | Carlos Eduardo Nunes |

### 4.2 Tipos de teste

- **Funcional:** todos os casos CT001 a CT010 verificam o comportamento definido pelos RF e RN.

- **Regressão:** a suíte automatizada completa é executada no pipeline de integração contínua a cada push na branch principal.

- **Não funcional:** desempenho da emissão do histórico (RNF001, CT010) e compatibilidade de navegadores (RNF003, CT001 e CT004 executados em Chrome e Firefox).

### 4.3 Técnicas de projeto de casos de teste

| **Técnica** | **Onde foi aplicada** |
| :-: | :-: |
| Partição de equivalência | Validação de CPF (CT001, CT002, CT003) e condições de matrícula (CT004, CT005) |
| Análise de valor limite | Capacidade da turma (CT006), limite de disciplinas (CT007) e intervalo da nota (CT008) |
| Tabela de decisão | Situação final combinando média e frequência (CT009) |
| Teste baseado em cenário | Emissão do histórico escolar com medição de tempo (CT010) |

## 5. Ambiente de testes

| **Sistema operacional** | Windows 11 e Ubuntu 24.04 (pipeline de integração contínua) |
| - | - |
| **Navegadores e versões** | Google Chrome e Mozilla Firefox, versões estáveis vigentes em outubro de 2026 |
| **Banco de dados** | PostgreSQL 16 com base dedicada sga\_teste; H2 em memória para testes de integração |
| **Ferramentas** | JUnit 5, Cypress 13, GitHub Actions (execução automática) e GitHub Issues (registro de defeitos) |
| **URL / servidor** | http://localhost:8080 (local) e ambiente de homologação do grupo |
| **Massa de dados** | Script seed-teste.sql recriado antes de cada execução, com 10 alunos, 6 disciplinas, 8 turmas e período de matrícula aberto |

## 6. Critérios

| **Critérios de entrada** | Build da Sprint 3 implantado no ambiente de testes; massa de dados carregada; casos de teste revisados por um integrante que não os escreveu |
| - | - |
| **Critérios de saída** | 100% dos casos de prioridade Alta aprovados; nenhum defeito de severidade Crítica ou Alta aberto; todos os requisitos da seção 2 cobertos ou com lacuna justificada |
| **Critérios de suspensão** | Falha na autenticação ou na conexão com o banco de dados que impeça a execução de mais de 50% dos casos |
| **Critérios de retomada** | Correção do defeito bloqueante implantada e validada por reteste do caso afetado |

## 7. Casos de teste

### 7.1 Resumo dos casos de teste

| **ID** | **Título** | **Requisitos** | **Prioridade** | **Automatizado** |
| :-: | :-: | :-: | :-: | :-: |
| CT001 | Cadastrar aluno com dados válidos | RF001, RN001, RN002, RNF003 | Alta | Sim |
| CT002 | Impedir cadastro de aluno com CPF já cadastrado | RF001, RN001 | Alta | Sim |
| CT003 | Validar formato e dígitos verificadores do CPF | RN001 | Alta | Sim |
| CT004 | Matricular aluno em disciplina com vaga e pré-requisito cumprido | RF002, RN003, RN004, RN005, RN008 | Alta | Sim |
| CT005 | Bloquear matrícula sem pré-requisito cumprido | RF002, RN004 | Alta | Sim |
| CT006 | Respeitar a capacidade máxima da turma | RF002, RN005 | Alta | Sim |
| CT007 | Respeitar o limite de disciplinas por semestre | RF002, RN003 | Média | Sim |
| CT008 | Validar intervalo da nota lançada | RF003, RN006 | Alta | Sim |
| CT009 | Calcular a situação final do aluno na disciplina | RF003, RN007 | Alta | Sim |
| CT010 | Emitir histórico escolar dentro do tempo de resposta | RF004, RNF001 | Média | Parcial |

### 7.2 Especificação dos casos de teste

| **CT001  Cadastrar aluno com dados válidos** |
| - |
| **Requisitos rastreados** | RF001, RN001, RN002, RNF003 |
| **Caso de uso relacionado** | UC01 Manter Cadastro de Alunos |
| **Nível / Tipo** | Sistema (E2E) / Funcional |
| **Técnica de projeto** | Partição de equivalência (classe válida para todos os campos) |
| **Prioridade** | Alta |
| **Pré-condições** | Usuário da secretaria autenticado; CPF 529.982.247-25 não cadastrado na base |
| **Dados de teste** | Nome: Mariana Souza Prado; CPF: 529.982.247-25; data de nascimento: 14/03/2004; e-mail: mariana.prado@email.com; curso: ADS |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Acessar o menu Alunos \> Novo aluno | Formulário de cadastro é exibido com todos os campos vazios |
| 2 | Preencher os campos com os dados de teste | Campos aceitam os valores sem mensagens de erro |
| 3 | Acionar o botão "Salvar" | Mensagem "Aluno cadastrado com sucesso" é exibida |
| 4 | Consultar o aluno pelo CPF informado | Aluno é listado com situação "Ativo" e RA gerado automaticamente no formato AAAASNNNN (RN002) |
| **Pós-condições** | Aluno cadastrado, ativo e com RA único |
| **Automatizado?** | Sim, Cypress (cypress/e2e/alunos/cadastro-valido.cy.js); executado em Chrome e Firefox (RNF003) |
| **Resultado da execução** | Aprovado em 09/10/2026 por Ana Beatriz Lima |

| **CT002  Impedir cadastro de aluno com CPF já cadastrado** |
| - |
| **Requisitos rastreados** | RF001, RN001 |
| **Caso de uso relacionado** | UC01 Manter Cadastro de Alunos (fluxo de exceção FE01) |
| **Nível / Tipo** | Sistema (E2E) / Funcional |
| **Técnica de projeto** | Partição de equivalência (classe inválida: CPF duplicado) |
| **Prioridade** | Alta |
| **Pré-condições** | Aluno com CPF 529.982.247-25 já cadastrado (resultado do CT001 ou massa de dados) |
| **Dados de teste** | Nome: Pedro Henrique Alves; CPF: 529.982.247-25; demais campos válidos |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Acessar o menu Alunos \> Novo aluno | Formulário de cadastro é exibido |
| 2 | Preencher os campos com os dados de teste e acionar "Salvar" | Mensagem "CPF já cadastrado" é exibida junto ao campo CPF |
| 3 | Consultar alunos pelo CPF informado | Apenas um registro é retornado (o cadastro original) |
| **Pós-condições** | Nenhum novo registro criado |
| **Automatizado?** | Sim, Cypress (cypress/e2e/alunos/cpf-duplicado.cy.js) |
| **Resultado da execução** | Aprovado em 09/10/2026 por Ana Beatriz Lima |

| **CT003  Validar formato e dígitos verificadores do CPF** |
| - |
| **Requisitos rastreados** | RN001 |
| **Caso de uso relacionado** | UC01 Manter Cadastro de Alunos |
| **Nível / Tipo** | Unitário / Funcional |
| **Técnica de projeto** | Partição de equivalência (uma classe válida e quatro inválidas) |
| **Prioridade** | Alta |
| **Pré-condições** | Classe ValidadorCpf disponível para teste isolado |
| **Dados de teste** | Ver tabela de partições abaixo |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Executar o método validar() com cada valor da tabela de partições | O retorno de cada chamada corresponde à coluna "Resultado esperado" da tabela |
| **Pós-condições** | Não se aplica (teste sem efeito colateral) |
| **Automatizado?** | Sim, JUnit (src/test/java/sga/aluno/ValidadorCpfTest.java) |
| **Resultado da execução** | Aprovado em 08/10/2026 por Lucas Martins |

**Tabela de partições do CPF**

| **Classe** | **Valor de entrada** | **Resultado esperado** |
| :-: | :-: | :-: |
| Válida: CPF com dígitos corretos | 529.982.247-25 | true |
| Inválida: dígito verificador incorreto | 529.982.247-26 | false |
| Inválida: todos os dígitos iguais | 111.111.111-11 | false |
| Inválida: quantidade de dígitos diferente de 11 | 529.982.247 | false |
| Inválida: caracteres não numéricos | 52A.982.247-25 | false |

| **CT004  Matricular aluno em disciplina com vaga e pré-requisito cumprido** |
| - |
| **Requisitos rastreados** | RF002, RN003, RN004, RN005, RN008 |
| **Caso de uso relacionado** | UC02 Realizar Matrícula em Disciplinas |
| **Nível / Tipo** | Sistema (E2E) / Funcional |
| **Técnica de projeto** | Partição de equivalência (todas as condições atendidas) |
| **Prioridade** | Alta |
| **Pré-condições** | Período de matrícula aberto (RN008); aluno RA 20262A0001 aprovado em Algoritmos; turma de Estrutura de Dados com 20 matriculados e capacidade 40; aluno com 3 disciplinas no semestre |
| **Dados de teste** | Aluno: RA 20262A0001; disciplina: Estrutura de Dados, turma A, noite |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Autenticar como aluno e acessar Matrícula | Lista de disciplinas disponíveis é exibida com a quantidade de vagas |
| 2 | Selecionar Estrutura de Dados, turma A, e acionar "Confirmar matrícula" | Mensagem "Matrícula realizada" é exibida |
| 3 | Acessar "Minhas disciplinas" | Estrutura de Dados consta na lista; total de disciplinas passa a 4 |
| **Pós-condições** | Matrícula registrada; vagas da turma reduzidas para 19 |
| **Automatizado?** | Sim, Cypress (cypress/e2e/matricula/matricula-valida.cy.js); executado em Chrome e Firefox (RNF003) |
| **Resultado da execução** | Aprovado em 10/10/2026 por Fernanda Oliveira |

| **CT005  Bloquear matrícula sem pré-requisito cumprido** |
| - |
| **Requisitos rastreados** | RF002, RN004 |
| **Caso de uso relacionado** | UC02 Realizar Matrícula em Disciplinas (fluxo de exceção FE02) |
| **Nível / Tipo** | Sistema (E2E) / Funcional |
| **Técnica de projeto** | Partição de equivalência (classe inválida: pré-requisito pendente) |
| **Prioridade** | Alta |
| **Pré-condições** | Período de matrícula aberto; aluno RA 20262A0002 sem aprovação em Algoritmos |
| **Dados de teste** | Aluno: RA 20262A0002; disciplina: Estrutura de Dados, turma A |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Autenticar como aluno e acessar Matrícula | Lista de disciplinas é exibida |
| 2 | Selecionar Estrutura de Dados e acionar "Confirmar matrícula" | Mensagem "Pré-requisito não cumprido: Algoritmos" é exibida e a matrícula não é efetivada |
| **Pós-condições** | Nenhuma matrícula registrada; vagas da turma inalteradas |
| **Automatizado?** | Sim, Cypress (cypress/e2e/matricula/sem-pre-requisito.cy.js) |
| **Resultado da execução** | Aprovado em 10/10/2026 por Fernanda Oliveira |

| **CT006  Respeitar a capacidade máxima da turma** |
| - |
| **Requisitos rastreados** | RF002, RN005 |
| **Caso de uso relacionado** | UC02 Realizar Matrícula em Disciplinas |
| **Nível / Tipo** | Integração / Funcional |
| **Técnica de projeto** | Análise de valor limite (capacidade de 40 alunos) |
| **Prioridade** | Alta |
| **Pré-condições** | Período de matrícula aberto; turma B de Banco de Dados com capacidade 40; alunos com pré-requisito cumprido |
| **Dados de teste** | Ver tabela de valores limite abaixo |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Preparar a turma com a quantidade de matriculados indicada em cada linha da tabela | Base de dados reflete a quantidade informada |
| 2 | Chamar o serviço de matrícula para um novo aluno | O resultado corresponde à coluna "Resultado esperado" |
| **Pós-condições** | Turma nunca ultrapassa 40 matriculados |
| **Automatizado?** | Sim, JUnit com banco em memória (src/test/java/sga/matricula/CapacidadeTurmaIT.java) |
| **Resultado da execução** | Reprovado em 10/10/2026 (DF001). Reexecutado e aprovado em 11/10/2026 por Lucas Martins |

**Valores limite da capacidade da turma**

| **Matriculados antes** | **Situação** | **Resultado esperado** |
| :-: | :-: | :-: |
| 38 | Abaixo do limite | Matrícula aceita (39 matriculados) |
| 39 | Imediatamente abaixo do limite | Matrícula aceita (40 matriculados) |
| 40 | No limite | Matrícula recusada com a mensagem "Turma sem vagas" |

| **CT007  Respeitar o limite de disciplinas por semestre** |
| - |
| **Requisitos rastreados** | RF002, RN003 |
| **Caso de uso relacionado** | UC02 Realizar Matrícula em Disciplinas |
| **Nível / Tipo** | Integração / Funcional |
| **Técnica de projeto** | Análise de valor limite (máximo de 7 disciplinas) |
| **Prioridade** | Média |
| **Pré-condições** | Período de matrícula aberto; turmas com vagas; pré-requisitos cumpridos |
| **Dados de teste** | Aluno com 6 disciplinas e aluno com 7 disciplinas matriculadas no semestre |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Solicitar nova matrícula para o aluno com 6 disciplinas | Matrícula aceita; aluno passa a ter 7 disciplinas |
| 2 | Solicitar nova matrícula para o aluno com 7 disciplinas | Matrícula recusada com a mensagem "Limite de 7 disciplinas atingido" |
| **Pós-condições** | Nenhum aluno com mais de 7 disciplinas no semestre |
| **Automatizado?** | Sim, JUnit (src/test/java/sga/matricula/LimiteDisciplinasIT.java) |
| **Resultado da execução** | Aprovado em 10/10/2026 por Lucas Martins |

| **CT008  Validar intervalo da nota lançada** |
| - |
| **Requisitos rastreados** | RF003, RN006 |
| **Caso de uso relacionado** | UC03 Lançar Notas e Frequência |
| **Nível / Tipo** | Unitário / Funcional |
| **Técnica de projeto** | Análise de valor limite (intervalo de 0,0 a 10,0 com uma casa decimal) |
| **Prioridade** | Alta |
| **Pré-condições** | Classe ValidadorNota disponível para teste isolado |
| **Dados de teste** | Ver tabela de valores limite abaixo |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Executar o método validar() com cada valor da tabela | O retorno de cada chamada corresponde à coluna "Resultado esperado" |
| **Pós-condições** | Não se aplica |
| **Automatizado?** | Sim, JUnit (src/test/java/sga/nota/ValidadorNotaTest.java) |
| **Resultado da execução** | Reprovado em 09/10/2026 (DF002). Correção em andamento |

**Valores limite da nota**

| **Valor** | **Situação** | **Resultado esperado** |
| :-: | :-: | :-: |
| -0,1 | Abaixo do limite inferior | Rejeitada |
| 0,0 | Limite inferior | Aceita |
| 0,1 | Acima do limite inferior | Aceita |
| 9,9 | Abaixo do limite superior | Aceita |
| 10,0 | Limite superior | Aceita |
| 10,1 | Acima do limite superior | Rejeitada |
| 7,25 | Mais de uma casa decimal | Rejeitada |

| **CT009  Calcular a situação final do aluno na disciplina** |
| - |
| **Requisitos rastreados** | RF003, RN007 |
| **Caso de uso relacionado** | UC03 Lançar Notas e Frequência |
| **Nível / Tipo** | Unitário / Funcional |
| **Técnica de projeto** | Tabela de decisão (média final e frequência) |
| **Prioridade** | Alta |
| **Pré-condições** | Classe CalculadoraSituacao disponível para teste isolado |
| **Dados de teste** | Ver tabela de decisão abaixo |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Executar o método calcular(media, frequencia) para cada regra da tabela | A situação retornada corresponde à linha "Ação" da regra |
| **Pós-condições** | Não se aplica |
| **Automatizado?** | Sim, JUnit parametrizado (src/test/java/sga/nota/CalculadoraSituacaoTest.java) |
| **Resultado da execução** | Aprovado em 09/10/2026 por Lucas Martins |

**Tabela de decisão da situação final (RN007)**

| **Condições e ações** | **Regra 1** | **Regra 2** | **Regra 3** | **Regra 4** |
| :-: | :-: | :-: | :-: | :-: |
| Média final maior ou igual a 6,0 | Sim | Sim | Não | Não |
| Frequência maior ou igual a 75% | Sim | Não | Sim | Não |
| Dados usados (média; frequência) | 6,0; 75% | 8,5; 74% | 5,9; 90% | 4,0; 50% |
| Ação: situação final | Aprovado | Reprovado por falta | Reprovado por nota | Reprovado por nota e falta |

| **CT010  Emitir histórico escolar dentro do tempo de resposta** |
| - |
| **Requisitos rastreados** | RF004, RNF001 |
| **Caso de uso relacionado** | UC04 Emitir Histórico Escolar |
| **Nível / Tipo** | Sistema / Funcional e Não funcional (desempenho) |
| **Técnica de projeto** | Teste baseado em cenário, com medição de tempo |
| **Prioridade** | Média |
| **Pré-condições** | Aluno RA 20241A0007 com 4 semestres concluídos e 24 disciplinas cursadas |
| **Dados de teste** | Aluno: RA 20241A0007 |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | Autenticar como aluno e acessar "Histórico escolar" | Tela de histórico é carregada |
| 2 | Acionar "Gerar PDF" e medir o tempo até a exibição do arquivo | PDF é exibido em no máximo 3 segundos (RNF001) |
| 3 | Conferir o conteúdo do PDF | Constam as 24 disciplinas, com semestre, nota final, frequência e situação de cada uma |
| **Pós-condições** | Nenhuma alteração na base de dados |
| **Automatizado?** | Parcial: tempo medido com Cypress (cypress/e2e/historico/tempo-emissao.cy.js); conferência do conteúdo feita manualmente |
| **Resultado da execução** | Aprovado em 11/10/2026 por Carlos Eduardo Nunes (tempo médio de 1,8 s em 5 execuções) |

## 8. Matriz de rastreabilidade

| **Requisito** | **Descrição resumida** | **Casos de teste** | **Situação** |
| :-: | :-: | :-: | :-: |
| RF001 | Manter cadastro de alunos | CT001, CT002 | Coberto |
| RF002 | Realizar matrícula em disciplinas | CT004, CT005, CT006, CT007 | Coberto |
| RF003 | Lançar notas e frequência | CT008, CT009 | Coberto |
| RF004 | Emitir histórico escolar | CT010 | Coberto |
| RN001 | CPF válido e único | CT001, CT002, CT003 | Coberto |
| RN002 | Geração automática do RA | CT001 | Coberto |
| RN003 | Máximo de 7 disciplinas por semestre | CT004, CT007 | Coberto |
| RN004 | Pré-requisito cumprido | CT004, CT005 | Coberto |
| RN005 | Capacidade máxima de 40 alunos | CT004, CT006 | Coberto |
| RN006 | Nota entre 0,0 e 10,0 | CT008 | Coberto |
| RN007 | Critério de aprovação | CT009 | Coberto |
| RN008 | Matrícula dentro do período | CT004 | Parcial: falta cenário fora do período (ver R4) |
| RNF001 | Histórico em até 3 segundos | CT010 | Coberto |
| RNF002 | Senhas com hash criptográfico | Nenhum | Lacuna: verificado por inspeção de código nesta sprint; caso de teste previsto para a Sprint 4 |
| RNF003 | Compatibilidade com Chrome e Firefox | CT001, CT004 | Coberto |

## 9. Cronograma e responsabilidades

As estimativas utilizam a técnica PERT, com E = (O + 4M + P) / 6, em horas. O total estimado é de aproximadamente 20,5 horas, distribuídas entre 05/10 e 12/10/2026.

| **Atividade** | **Responsável** | **O** | **M** | **P** | **E (h)** | **Prazo** |
| :-: | :-: | :-: | :-: | :-: | :-: | :-: |
| Projeto dos casos de teste | Ana Beatriz Lima | 2 | 4 | 8 | 4,3 | 06/10 |
| Preparação do ambiente e da massa de dados | Carlos Eduardo Nunes | 1 | 2 | 5 | 2,3 | 07/10 |
| Implementação dos testes automatizados | Lucas Martins e Fernanda Oliveira | 4 | 8 | 14 | 8,3 | 09/10 |
| Execução e registro de resultados | Fernanda Oliveira | 2 | 3 | 6 | 3,3 | 10/10 |
| Reteste e regressão | Lucas Martins | 1 | 2 | 4 | 2,2 | 12/10 |

## 10. Riscos e contingências

| **ID** | **Risco** | **Probabilidade** | **Impacto** | **Ação de contingência** |
| :-: | :-: | :-: | :-: | :-: |
| R1 | Atraso na entrega da funcionalidade de lançamento de notas | Média | Alto | Iniciar pelos testes unitários das classes de validação, que não dependem da interface |
| R2 | Instabilidade do ambiente de homologação | Baixa | Médio | Executar a suíte localmente com a mesma massa de dados |
| R3 | Ausência de requisito de volume de usuários simultâneos no DRS | Alta | Médio | Inconsistência comunicada ao professor em 05/10/2026; testes de carga fora do escopo até definição |
| R4 | RN008 não informa se a matrícula já iniciada pode ser concluída após o fim do período | Média | Médio | Ambiguidade comunicada ao professor; cenário fora do período será especificado após o esclarecimento |

## 11. Gestão de defeitos

### 11.1 Classificação de severidade

| **Severidade** | **Critério** |
| :-: | :-: |
| Crítica | Impede o uso de uma funcionalidade essencial, sem alternativa de contorno (ex.: aluno não consegue se matricular) |
| Alta | Funcionalidade com resultado incorreto, com alternativa de contorno |
| Média | Falha em regra secundária ou mensagem incorreta |
| Baixa | Problema cosmético (layout, texto, alinhamento) |

### 11.2 Registro de defeitos

Os defeitos são registrados no GitHub Issues do repositório do grupo com o rótulo "defeito" e referenciados pelo identificador abaixo.

| **ID** | **Caso de teste** | **Descrição** | **Severidade** | **Situação** |
| :-: | :-: | :-: | :-: | :-: |
| DF001 | CT006 | Com 40 alunos matriculados, a turma aceitou a 41ª matrícula. A verificação usava "maior que 40" em vez de "maior ou igual a 40" | Alta | Corrigido e fechado em 11/10 |
| DF002 | CT008 | A nota 10,0 é rejeitada. O validador trata o limite superior como exclusivo | Alta | Aberto |

Como o DF002 é de severidade Alta e permanece aberto, o critério de saída ainda não foi atingido. A equipe registrou o bloqueio na revisão diária e a correção está prevista para 13/10/2026, seguida de reteste do CT008.

## 12. Entregáveis

- Este Plano de Testes, versão 1.1.

- Scripts de testes automatizados nas pastas src/test/java e cypress/e2e do repositório do grupo.

- Relatório de execução do GitHub Actions e vídeos gerados pelo Cypress para os casos E2E.

- Registro dos defeitos no GitHub Issues.
