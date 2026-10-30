# Plano de Testes

![](imagens/template-plano-de-testes-01.png)

**\[Nome do Projeto\]**

**\[OPCIONAL\]**

**Histórico de Versões**

| **Data** | **Versão** | **Descrição** | **Autor** | **Revisor** |
| :-: | :-: | :-: | :-: | :-: |
| \[dd/mm/aa\] | \[x.y\] | \[Uma breve descrição da versão em questão\] | \[Nome do primeiro autor\] | \[Nome dos possíveis revisores da versão\] |
|  |  |  |  |  |
|  |  |  |  |  |
|  |  |  |  |  |
|  |  |  |  |  |

| **Cliente** | \[Nome do cliente\] |
| -: | - |
| **Documento** | Plano de Testes: \[Nome do Projeto\] |
| **Data** | \[dd de mês de aaaa\] |
| **Autor(es)** | \[Nome completo e RA de cada integrante do grupo\] |
| **Documento base** | \[DRS e versão utilizada\] |

**Página de Assinaturas**

| Revisado e Aprovado por: |  |
| - | - |
|  | dd.mm.aa |
| Revisado e Aprovado por: |  |
|  | dd.mm.aa |
| Revisado e Aprovado por: |  |
|  | dd.mm.aa |

**Índice**

*Para atualizar o índice no Word, clique com o botão direito sobre ele e escolha "Atualizar campo".*

## 1. Introdução

### 1.1 Objetivo

\[Descreva o propósito deste plano: o que será verificado, em qual incremento do produto e qual decisão ele apoia (por exemplo, a liberação da sprint). Um único parágrafo é suficiente.\]

\[Texto\]

### 1.2 Escopo

\[Resuma o que está coberto por este plano. O detalhamento item a item fica nas seções 2 e 3.\]

\[Texto\]

### 1.3 Documentos de referência

| **Documento** | **Versão** | **Observação** |
| :-: | :-: | :-: |
| \[Documento de Requisitos de Software (DRS)\] | \[x.y\] | \[Fonte dos requisitos rastreados\] |
| \[Especificações de Casos de Uso\] | \[x.y\] | \[Casos de uso cobertos\] |
| \[Documento de Visão do Projeto\] | \[x.y\] | \[Opcional\] |

### 1.4 Definições e siglas

| **Termo** | **Definição** |
| :-: | :-: |
| RF / RN / RNF | Requisito Funcional / Regra de Negócio / Requisito Não Funcional |
| CT | Caso de Teste |
| E2E | End to End: teste que percorre o fluxo completo pela interface do usuário |
| \[Termo\] | \[Definição\] |

## 2. Itens a serem testados

\[Liste as funcionalidades cobertas, sempre com os identificadores do DRS. Não inclua requisitos que não existam no documento de requisitos.\]

| **Funcionalidade** | **Requisitos (DRS)** | **Caso de uso** | **Prioridade** |
| :-: | :-: | :-: | :-: |
| \[Funcionalidade\] | \[RF, RN e RNF\] | \[UC\] | \[Alta\] |
| \[Funcionalidade\] | \[RF, RN e RNF\] | \[UC\] | \[Média\] |

## 3. Itens fora do escopo

\[Declare explicitamente o que NÃO será testado neste ciclo e o motivo. Esta seção evita ambiguidade na avaliação.\]

| **Item** | **Motivo da exclusão** |
| :-: | :-: |
| \[Item\] | \[Motivo\] |
| \[Item\] | \[Motivo\] |

## 4. Estratégia de testes

### 4.1 Níveis de teste

\[Indique quais níveis serão aplicados, com que ferramenta e quem é o responsável. Remova as linhas que não se aplicam.\]

| **Nível** | **Objetivo** | **Ferramenta** | **Responsável** |
| :-: | :-: | :-: | :-: |
| Unitário | Verificar regras de negócio isoladas (validações, cálculos) | \[JUnit, Jest...\] | \[Nome\] |
| Integração | Verificar a comunicação entre camadas (controller, serviço, banco de dados) | \[Ferramenta\] | \[Nome\] |
| Sistema (E2E) | Verificar os fluxos completos pela interface | \[Selenium ou Cypress\] | \[Nome\] |
| Aceitação | Validar o atendimento aos requisitos junto ao cliente (professor) | Execução manual | \[Nome\] |

### 4.2 Tipos de teste

- **Funcional:** verifica se o sistema faz o que os RF e RN determinam.

- **Regressão:** reexecuta casos já aprovados após cada alteração para garantir que nada deixou de funcionar.

- **Não funcional:** \[desempenho, usabilidade, segurança, conforme os RNF do DRS\]

### 4.3 Técnicas de projeto de casos de teste

\[Explique como os casos foram derivados. Para cada técnica usada, cite ao menos um exemplo aplicado ao sistema.\]

| **Técnica** | **Onde foi aplicada** |
| :-: | :-: |
| Partição de equivalência | \[Campo ou regra; classes válidas e inválidas\] |
| Análise de valor limite | \[Campo, limite inferior e superior\] |
| Tabela de decisão | \[Regra com múltiplas condições combinadas\] |
| \[Outra\] | \[Aplicação\] |

## 5. Ambiente de testes

| **Sistema operacional** | \[Ex.: Windows 11, Ubuntu 24.04\] |
| - | - |
| **Navegadores e versões** | \[Ex.: Chrome, Firefox e versões\] |
| **Banco de dados** | \[SGBD e versão; base dedicada a testes\] |
| **Ferramentas** | \[Selenium ou Cypress, framework de testes unitários, gerenciador de defeitos\] |
| **URL / servidor** | \[Endereço do ambiente de testes\] |
| **Massa de dados** | \[Como os dados de teste são criados e restaurados (scripts, seeds)\] |

## 6. Critérios

\[Os critérios devem ser objetivos e verificáveis. Prefira "100% dos casos de prioridade Alta aprovados" a "o sistema estar funcionando bem".\]

| **Critérios de entrada** | \[Condições para iniciar os testes\] |
| - | - |
| **Critérios de saída** | \[Condições para encerrar os testes\] |
| **Critérios de suspensão** | \[Quando interromper a execução\] |
| **Critérios de retomada** | \[Quando voltar a testar\] |

## 7. Casos de teste

### 7.1 Resumo dos casos de teste

| **ID** | **Título** | **Requisitos** | **Prioridade** | **Automatizado** |
| :-: | :-: | :-: | :-: | :-: |
| CT001 | \[Título\] | \[RF, RN\] | \[Alta\] | \[Sim\] |
| CT002 | \[Título\] | \[RF, RN\] | \[Média\] | \[Não\] |
| CT003 | \[Título\] | \[RF, RN\] | \[Alta\] | \[Sim\] |

### 7.2 Especificação dos casos de teste

\[Replique a ficha abaixo para cada caso de teste listado no resumo. Cubra o fluxo principal e os cenários de erro (dados inválidos, campos obrigatórios vazios, violação de regra de negócio). Consulte o documento de exemplo para o nível de detalhe esperado.\]

| **CT001  \[Título do caso de teste\]** |
| - |
| **Requisitos rastreados** | \[RF, RN e RNF cobertos, conforme o DRS\] |
| **Caso de uso relacionado** | \[Identificador e nome do caso de uso\] |
| **Nível / Tipo** | \[Unitário, Integração, Sistema ou Aceitação / Funcional, Regressão ou Não funcional\] |
| **Técnica de projeto** | \[Partição de equivalência, Valor limite, Tabela de decisão...\] |
| **Prioridade** | \[Alta, Média ou Baixa\] |
| **Pré-condições** | \[Estado do sistema antes da execução\] |
| **Dados de teste** | \[Valores concretos de entrada\] |
| **Passo** | **Ação** | **Resultado esperado** |
| 1 | \[Ação executada\] | \[Resultado esperado\] |
| 2 | \[Ação executada\] | \[Resultado esperado\] |
| 3 | \[Ação executada\] | \[Resultado esperado\] |
| **Pós-condições** | \[Estado esperado após a execução\] |
| **Automatizado?** | \[Sim ou Não; ferramenta e caminho do script\] |
| **Resultado da execução** | \[Aprovado, Reprovado ou Bloqueado; data; responsável; ID do defeito, se houver\] |

## 8. Matriz de rastreabilidade

\[Todo requisito listado na seção 2 deve aparecer aqui com pelo menos um caso de teste. Requisito sem caso de teste é uma lacuna de cobertura e deve ser justificado.\]

| **Requisito** | **Descrição resumida** | **Casos de teste** | **Situação** |
| :-: | :-: | :-: | :-: |
| \[RF\] | \[Descrição conforme o DRS\] | \[CT001, CT002\] | \[Coberto\] |
| \[RN\] | \[Descrição conforme o DRS\] | \[CT003\] | \[Coberto\] |
| \[RNF\] | \[Descrição conforme o DRS\] | \[Nenhum\] | \[Lacuna: justificar\] |

## 9. Cronograma e responsabilidades

\[Distribua as atividades de teste ao longo da sprint. Use a técnica PERT: E = (O + 4M + P) / 6, onde O é a estimativa otimista, M a mais provável e P a pessimista, em horas.\]

| **Atividade** | **Responsável** | **O** | **M** | **P** | **E (h)** | **Prazo** |
| :-: | :-: | :-: | :-: | :-: | :-: | :-: |
| Projeto dos casos de teste | \[Nome\] |  |  |  |  | \[dd/mm\] |
| Preparação do ambiente e da massa de dados | \[Nome\] |  |  |  |  | \[dd/mm\] |
| Implementação dos testes automatizados | \[Nome\] |  |  |  |  | \[dd/mm\] |
| Execução e registro de resultados | \[Nome\] |  |  |  |  | \[dd/mm\] |
| Reteste e regressão | \[Nome\] |  |  |  |  | \[dd/mm\] |

## 10. Riscos e contingências

\[Inclua também eventuais ambiguidades ou inconsistências identificadas no DRS, informando que foram comunicadas ao professor.\]

| **Risco** | **Probabilidade** | **Impacto** | **Ação de contingência** |
| :-: | :-: | :-: | :-: |
| \[Risco\] | \[Alta, Média ou Baixa\] | \[Alto, Médio ou Baixo\] | \[Ação\] |
| \[Risco\] |  |  | \[Ação\] |

## 11. Gestão de defeitos

### 11.1 Classificação de severidade

| **Severidade** | **Critério** |
| :-: | :-: |
| Crítica | Impede o uso de uma funcionalidade essencial, sem alternativa de contorno |
| Alta | Funcionalidade com resultado incorreto, com alternativa de contorno |
| Média | Falha em regra secundária ou mensagem incorreta |
| Baixa | Problema cosmético (layout, texto, alinhamento) |

### 11.2 Registro de defeitos

\[Informe onde os defeitos serão registrados (ex.: GitHub Issues, Jira, planilha) e registre abaixo os encontrados durante a execução.\]

| **ID** | **Caso de teste** | **Descrição** | **Severidade** | **Situação** |
| :-: | :-: | :-: | :-: | :-: |
| DF001 | \[CT\] | \[Comportamento observado x esperado\] | \[Alta\] | \[Aberto, Corrigido ou Fechado\] |

## 12. Entregáveis

- Este Plano de Testes preenchido.

- Scripts de testes automatizados versionados no repositório do grupo.

- Evidências de execução (relatório da ferramenta, capturas de tela ou vídeo).

- \[Outros entregáveis definidos pelo grupo\]
