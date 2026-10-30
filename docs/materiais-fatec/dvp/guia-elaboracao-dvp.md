# GUIA DE ELABORAÇÃO DO Documento de Visão de Projeto (DVP)

*Como preencher o template, seção por seção*

## 1. Por que o Documento de Visão de Projeto importa

Antes de qualquer linha de código ser escrita, todo projeto de software precisa responder a uma pergunta básica, mas frequentemente negligenciada: "Por que este projeto existe e o que exatamente ele deve entregar?" O Documento de Visão de Projeto (DVP) é o artefato que registra essa resposta.

O DVP é normalmente o primeiro documento formal de um projeto. Ele é elaborado ainda na fase de iniciação, quando o time ainda não tem todos os detalhes técnicos definidos — e isso é proposital. O objetivo aqui não é especificar cada tela ou cada regra de negócio (isso será feito depois, no Documento de Requisitos), mas sim alinhar as expectativas de todos os envolvidos sobre o problema que está sendo resolvido, para quem e dentro de quais limites.

O DVP é importante porque ele cumpre, ao mesmo tempo, vários papéis:

- Alinha expectativas: garante que professor, equipe e (quando houver) cliente/stakeholder entendam o projeto da mesma forma, evitando retrabalho por mal-entendidos.

- Justifica o projeto: explicita a necessidade de negócio que motiva a solução, por que ela vale a pena ser construída.

- Define fronteiras: deixa claro o que está dentro e o que está fora do escopo, evitando que o projeto "cresça" descontroladamente ao longo do semestre.

- Orienta decisões técnicas futuras: a Representação Arquitetural e as visões (lógica, dados, implantação) descritas aqui servem de referência para as decisões de design que virão nas próximas fases.

- Serve de base para outros documentos: o Documento de Requisitos, o WBS e o cronograma detalhado do projeto vão se apoiar no que está definido aqui.

Em resumo: um DVP malfeito, copiado ou genérico compromete a qualidade de tudo que vem depois, porque as próximas entregas do projeto herdam suas fragilidades. Por isso, tratem esta primeira entrega com o mesmo cuidado que teriam com o código-fonte do projeto.

## 2. Como usar este guia

Vocês receberam um arquivo de template (DVP.docx) com a estrutura padrão do documento, incluindo exemplos ilustrativos (baseados em um sistema fictício de biblioteca/CRM) e trechos de instrução destacados. Este guia acompanha o template seção por seção, explicando:

- O objetivo de cada seção — por que ela existe e que pergunta ela responde;

- O que efetivamente deve ser escrito ali, com base no projeto que vocês estão desenvolvendo;

- Cuidados específicos para não cair em armadilhas comuns, especialmente a de copiar o conteúdo de exemplo.

| **⚠ Regra mais importante deste guia** O template contém exemplos (sistema de biblioteca, empresas fictícias como Houter/Premium, tecnologias como Struts/Hibernate/Oracle) apenas para ilustrar o nível de detalhe esperado. Esses exemplos NÃO devem aparecer no documento final de vocês. Cada frase, cada diagrama e cada decisão arquitetural registrada no DVP deve refletir o projeto real que sua equipe está desenvolvendo — inclusive quando a tecnologia, os stakeholders ou as regras de negócio forem diferentes dos exemplos. Um DVP que apenas substitui nomes no texto do exemplo, mas mantém a estrutura de frases e as mesmas decisões técnicas, será considerado cópia, não elaboração própria. |
| - |

## 3. Estrutura do template, seção por seção

### 3.1 Capa, Histórico de Versões e Página de Assinaturas

**Objetivo da seção:** Identificar o projeto, controlar as versões do documento ao longo do semestre e formalizar quem revisou e aprovou o conteúdo.

**O que vocês devem escrever:** Substituam "ImoX" pelo nome do projeto de vocês. Preencham a tabela de Histórico de Versões sempre que o documento for revisado de forma relevante (ex.: v1.0 na entrega inicial, v1.1 após incorporar feedback do professor). Preencham também os dados de Cliente, Documento, Data e Autor(es), aqui "Cliente" pode ser "FATEC - Interno" ou o nome do professor/disciplina, conforme orientação da turma.

### 3.2 Objetivo

**Objetivo da seção:** explicar por que este documento existe e o que pretende alinhar entre os interessados no projeto.

**O que vocês devem escrever:** Descrevam, em um ou dois parágrafos, que o DVP formaliza o início do projeto de vocês, apresentando uma visão geral e arquitetônica da solução a ser desenvolvida ao longo da disciplina. Mencionem o nome do projeto e, brevemente, o domínio do problema (ex.: gestão de vendas de livros, gestão de uma clínica, etc., o domínio real do projeto de vocês).

| **⚠ Atenção — não copiem o template** Não copiem a frase sobre "sistemas da linha SISCAD" — isso é específico do exemplo original e não existe no projeto de vocês. |
| - |

### 3.3 Escopo

**Objetivo da seção:** Delimitar, em alto nível, o que este documento cobre (não confundir com o escopo do projeto, que é tratado em outra seção).

**O que vocês devem escrever:** Expliquem que este documento trata da visão inicial do sistema que a equipe vai desenvolver, indicando que o detalhamento de requisitos, as regras de negócio e a arquitetura completa virão em documentos posteriores (Documento de Requisitos, modelagem de banco de dados, etc.).

### 3.4 Referências

**Objetivo da seção:** registrar quais documentos ou fontes foram usados para construir o DVP e quais documentos futuros dele dependerão.

**O que vocês devem escrever:** Listem reuniões com o professor/orientador, materiais da disciplina, conversas com colegas que simulam o papel de cliente, e qualquer material de apoio real que a equipe usou. Indiquem que este documento influencia o Documento de Requisitos, bem como o planejamento e o cronograma do projeto.

| **⚠ Atenção — não copiem o template** Não deixem "SISTEMA FRONT END - CRM 2.doc" ou qualquer referência do exemplo — se vocês não tiveram essa reunião ou não usaram esse documento, ele não deve aparecer aqui. |
| - |

### 3.5 Necessidades de Negócio

**Objetivo da seção:** Explicar o problema real que motiva a criação do sistema, a dor que ele resolve, do ponto de vista de quem usaria a solução no dia a dia.

**O que vocês devem escrever:** Descrevam, com base no projeto de vocês, qual necessidade prática justifica a existência do sistema. Pensem: se o sistema não existisse, que problema continuaria acontecendo? Quem sofreria com isso?

| **⚠ Atenção — não copiem o template** Os "Exemplo 1" e "Exemplo 2" do template falam de uma biblioteca. Usem-nos apenas para entender o nível de detalhe e o tom de escrita esperado — a necessidade de negócio descrita deve ser a do projeto de vocês, escrita com as próprias palavras da equipe. |
| - |

### 3.6 Objetivo do Projeto

**Objetivo da seção:** Traduzir a necessidade de negócio em um objetivo concreto de desenvolvimento: o que o sistema vai fazer para resolver o problema descrito na seção anterior.

**O que vocês devem escrever:** Listem, de forma objetiva, as principais capacidades que o sistema deve oferecer (ex.: "controlar o cadastro de produtos", "processar pedidos de venda", "gerar relatórios gerenciais"), fechando com uma frase sobre o benefício esperado (redução de tempo, redução de erros, melhoria de atendimento, etc.).

| **⚠ Atenção — não copiem o template** Não usem a lista de bullets do exemplo (armazenar em banco de dados, protocolo HTTP, rodar em navegador) como se fosse genérica o suficiente para qualquer projeto — ela deve refletir as capacidades específicas do sistema de vocês. |
| - |

### 3.7 Declaração Preliminar de Escopo

**Objetivo da seção:** apresentar uma visão de alto nível do que será entregue no projeto, sem entrar em detalhes finos (que ficam para o Documento de Requisitos).

**O que vocês devem escrever:** Nesta seção introdutória, apenas contextualizem que o escopo aqui é preliminar e será refinado posteriormente.

#### 3.7.1 Descrição

**Objetivo da seção:** Apresentar uma narrativa breve do produto do projeto, como se estivesse explicando o sistema para alguém de fora da equipe, em poucas frases.

**O que vocês devem escrever:** Escrevam um parágrafo narrativo descrevendo o sistema de vocês: o que ele é, quem usa e o que ele permite fazer.

#### 3.7.2 Produtos a serem entregues

**Objetivo da seção:** listar os artefatos concretos que serão entregues ao final da etapa do projeto (não apenas o software, mas também a documentação e a infraestrutura, se aplicável).

**O que vocês devem escrever:** Listem os entregáveis reais da disciplina: o sistema implementado (indicando a etapa/fase), os documentos de especificação (requisitos, este próprio DVP, diagramas), e informações sobre hospedagem/ambiente de implantação, se houver.

#### 3.7.3 Requisitos (visão geral)

**Objetivo da seção:** Registrar, em alto nível, os requisitos já conhecidos nesta fase inicial — sabendo que ainda serão detalhados no Documento de Requisitos.

**O que vocês devem escrever:** Apresentem uma lista curta de requisitos de alto nível, como ponto de partida para o documento de requisitos completo.

##### 3.7.3.1 Requisitos Funcionais

**Objetivo da seção:** Descrever, em alto nível, o que o sistema deve ser capaz de fazer do ponto de vista funcional.

**O que vocês devem escrever:** Escrevam frases no padrão "O sistema deve ser capaz de...", cobrindo as funcionalidades centrais do projeto de vocês (cadastros, processos de negócio, relatórios, etc.). Aqui o nível de detalhe é alto, o detalhamento completo (fluxos, exceções, regras) fica para o Documento de Requisitos.

| **⚠ Atenção — não copiem o template** As frases sobre "cadastro de livros", "controle de empréstimos" e "atraso na devolução" são do exemplo de biblioteca. Reescrevam completamente pensando nas funcionalidades reais do sistema de vocês — não adaptem apenas trocando substantivos. |
| - |

##### 3.7.3.2 Requisitos Não Funcionais

**Objetivo da seção:** Registrar restrições de qualidade, de tecnologia ou de ambiente que o sistema deve respeitar, independentemente das funcionalidades específicas.

**O que vocês devem escrever:** Indiquem a linguagem de programação, banco de dados, padrão arquitetural (ex.: MVC) e navegadores/plataformas suportados que a equipe realmente vai usar no projeto.

| **⚠ Atenção — não copiem o template** Não copiem "Java", "PostgreSQL" ou os navegadores do exemplo se essas não forem as tecnologias reais escolhidas pela equipe. |
| - |

##### 3.7.3.3 Cronograma de Marcos Sumariado / Regras de Negócio

**Objetivo da seção:** apresentar uma visão inicial dos marcos de entrega e de eventuais regras de negócio já conhecidas nesta fase.

**O que vocês devem escrever:** Se a equipe já tiver marcos de entrega definidos pela disciplina, resumam-nos aqui. Registrem também as regras de negócio já claras nesta fase inicial (o detalhamento completo fica no Documento de Requisitos).

### 3.8 Premissas

**Objetivo da seção:** registrar o que a equipe considera verdadeiro para planejar o projeto, mesmo sem certeza absoluta.

**O que vocês devem escrever:** Listem as premissas reais da equipe: quem orienta o projeto, disponibilidade do ambiente de desenvolvimento, acesso aos dados de teste, disponibilidade dos integrantes, prazos da disciplina, etc.

| **⚠ Atenção — não copiem o template** "O projeto será orientado pelo professor Rodrigo Rocha" é do exemplo — substituam pelo nome real do professor/orientador da turma de vocês e acrescentem as premissas específicas do projeto. |
| - |

### 3.9 Influência das Partes Interessadas

**Objetivo da seção:** Identificar quem tem interesse ou poder de influência sobre o projeto, e o que cada um espera dele, útil para antecipar conflitos e prioridades.

**O que vocês devem escrever:** Para o contexto acadêmico, isso pode incluir: o professor/orientador (interessado na aderência aos requisitos da disciplina e nos prazos), o "cliente simulado" (se houver um papel definido em sala), colegas que dependem da entrega para outras disciplinas integradas, e os próprios integrantes da equipe em seus diferentes papéis.

| **⚠ Atenção — não copiem o template** A lista de "Rodinei", "Pedro Mendes Junior", "Afonso Santos Filho" etc. é fictícia. Substituam por interessados reais do contexto de vocês — mesmo que sejam papéis simulados em sala de aula, descrevam o interesse específico de cada um em relação ao projeto de vocês. |
| - |

### 3.10 Representação Arquitetural

**Objetivo da seção:** Apresentar, em alto nível, como o sistema será estruturado tecnicamente — as camadas, padrões e tecnologias que sustentam a solução.

**O que vocês devem escrever:** Descrevam a arquitetura que a equipe realmente vai adotar (ex.: arquitetura em camadas, cliente-servidor, API REST + SPA, etc.), indicando o papel de cada camada (apresentação, negócio/domínio, persistência) e os frameworks/bibliotecas escolhidos. Insiram um diagrama arquitetural elaborado pela equipe (pode ser um diagrama de componentes ou um diagrama de camadas simples) que represente a arquitetura real do projeto.

| **⚠ Atenção — não copiem o template** O diagrama do template (Figura 1 - Modelo Arquitetural Genérico) e as referências a JSF, EJB, padrão EJB COMMAND são específicos do exemplo original. O diagrama de vocês precisa ser desenhado com base nas tecnologias e na estrutura real do sistema que estão construindo — não reutilizem a imagem do template. |
| - |

#### 3.10.1 Restrições Arquiteturais

**Objetivo da seção:** Registrar decisões técnicas que já são obrigatórias para o projeto, seja por definição da disciplina, seja por escolha da equipe.

**O que vocês devem escrever:** Listem as restrições reais: versão de linguagem/framework exigida, SGBD escolhido, servidor de aplicação/hospedagem, bibliotecas obrigatórias, etc.

| **⚠ Atenção — não copiem o template** "JDK 1.4", "Oracle Application Server", "Struts", "Spring", "Hibernate", "Oracle" são as escolhas do exemplo — substituam pelas tecnologias reais da stack escolhida por vocês. |
| - |

#### 3.10.2 Objetivos e Restrições Arquiteturais

**Objetivo da seção:** Detalhar requisitos específicos que impactam diretamente as decisões de arquitetura (ex.: integração com sistemas externos, exigências de segurança).

**O que vocês devem escrever:** Se o projeto de vocês precisar se integrar a algum sistema externo, seguir algum padrão de segurança específico ou usar software livre por exigência da disciplina, registrem aqui.

### 3.11 Visão de Use Case

**Objetivo da seção:** Apresentar os casos de uso arquiteturalmente mais significativos do sistema — aqueles que mais influenciam decisões técnicas (por estenderem ou incluírem outros casos de uso ou por acessarem sistemas externos).

**O que vocês devem escrever:** Não é necessário (nem esperado) representar todos os casos de uso do sistema aqui — apenas os mais relevantes do ponto de vista arquitetural.

#### 3.11.1 Diagrama de Caso de Uso Geral do Sistema

**Objetivo da seção:** apresentar uma visão visual dos principais atores e funcionalidades do sistema e de como se relacionam entre si.

**O que vocês devem escrever:** Elaborem um diagrama de caso de uso UML real, com os atores e os casos de uso do projeto de vocês (ex.: Cliente, Administrador, funcionalidades de cadastro, venda, troca/devolução, etc., conforme o escopo do sistema de vocês).

| **⚠ Atenção — não copiem o template** O diagrama "Caso de Uso Final" do template é apenas ilustrativo e pertence a outro sistema. O diagrama entregue por vocês deve ser modelado a partir do escopo real do projeto — não é para reaproveitar a imagem nem a estrutura exata dela. |
| - |

#### 3.11.2 Descrição dos Casos de Uso Arquiteturalmente Significativos

**Objetivo da seção:** detalhar, em texto, o que ocorre nos casos de uso mais importantes identificados no diagrama.

**O que vocês devem escrever:** Para cada caso de uso significativo, escrevam um parágrafo curto descrevendo quando ele se inicia, o que o ator faz e o que o sistema realiza em resposta.

| **⚠ Atenção — não copiem o template** "Manter Produtos" é o exemplo do template — substituam pelos casos de uso reais e significativos do sistema de vocês. |
| - |

### 3.12 Visão de Lógica

**Objetivo da seção:** Mostrar como o sistema está organizado internamente em pacotes e camadas, do ponto de vista do design.

**O que vocês devem escrever:** Apresentem o diagrama de camadas real do sistema de vocês (ex.: apresentação, negócio, persistência, ou as camadas equivalentes da arquitetura escolhida) e expliquem a responsabilidade de cada uma.

| **⚠ Atenção — não copiem o template** As figuras e descrições de camadas do template referem-se à arquitetura J2EE do exemplo (Struts, EJB, etc.). Seus diagramas de camadas e pacotes devem refletir a estrutura de pastas/módulos real do código do projeto de vocês. |
| - |

#### 3.12.1 Camada de Apresentação

**Objetivo da seção:** Detalhar os pacotes/módulos responsáveis pela interface com o usuário.

**O que vocês devem escrever:** Descrevam como a camada de apresentação do projeto de vocês está organizada (ex.: componentes de front-end, controllers de API, etc.) e insiram o diagrama de pacotes correspondente.

#### 3.12.2 Camada de Negócio

**Objetivo da seção:** Detalhar os pacotes/módulos responsáveis pelas regras de negócio do sistema.

**O que vocês devem escrever:** Descrevam a organização real da camada de negócio/domínio do projeto (services, use cases, controllers de negócio, etc.).

##### 3.12.2.1 Pacote Controller / 3.12.2.2 Pacote Model

**Objetivo da seção:** Detalhar, quando fizer sentido para a arquitetura escolhida, as classes de controle e de modelo/domínio mais relevantes.

**O que vocês devem escrever:** Insiram diagramas de classes reais do projeto (ainda que preliminares) que mostrem as principais classes de controle e de modelo do sistema de vocês.

| **⚠ Atenção — não copiem o template** Os diagramas de classes do template pertencem ao sistema de exemplo. Modelem as classes reais do domínio do projeto de vocês. |
| - |

#### 3.12.3 Camada de Persistência

**Objetivo da seção:** Detalhar como o sistema acessa e persiste dados.

**O que vocês devem escrever:** Descrevam a estratégia de persistência real (ORM, driver, padrão Repository/DAO, etc.) usada no projeto e insiram o diagrama de pacotes correspondente.

#### 3.12.4 Realização dos Casos de Uso Significativos

**Objetivo da seção:** Mostrar, tecnicamente, como as classes das diferentes camadas colaboram entre si para realizar um caso de uso significativo (geralmente com um diagrama de sequência ou de comunicação).

**O que vocês devem escrever:** Escolham um dos casos de uso significativos identificados na Visão de Use Case e representem, com um diagrama de sequência/colaboração, como ele é realizado pelas classes do sistema de vocês.

### 3.13 Visão de Implantação

**Objetivo da seção:** Descrever a infraestrutura física/lógica onde o sistema será executado, e como os componentes se conectam entre si.

**O que vocês devem escrever:** Elaborem um diagrama de implantação (deployment) real do projeto de vocês, indicando servidores, serviços de nuvem, bancos de dados e como os componentes se comunicam (ex.: navegador do cliente, servidor de aplicação, banco de dados, serviços externos).

| **⚠ Atenção — não copiem o template** O diagrama de implantação do template descreve a infraestrutura do sistema de exemplo (WCA, Servidor DB). O de vocês deve representar onde e como o sistema de vocês realmente roda ou rodará. |
| - |

### 3.14 Visão de Implementação

**Objetivo da seção:** Explicar a estrutura geral de implementação do software, relacionando-a à Visão de Lógica já apresentada.

**O que vocês devem escrever:** Indiquem se a estrutura de implementação (pastas, módulos, camadas de código) segue diretamente a Visão de Lógica descrita anteriormente ou destaquem as diferenças relevantes.

### 3.15 Visão de Dados

**Objetivo da seção:** apresentar o modelo de dados do sistema, tanto em nível lógico quanto em nível físico.

**O que vocês devem escrever:** Insiram o Diagrama de Entidade-Relacionamento (DER) ou modelo de classes de domínio (lógico) e, se possível, o modelo físico de banco de dados (com tipos de dados, chaves, etc.) do projeto de vocês. Indiquem também o SGBD e o mecanismo de persistência/ORM escolhidos.

| **⚠ Atenção — não copiem o template** Os modelos lógico e físico do template pertencem ao sistema SISCAD do exemplo. O modelo de dados entregue precisa corresponder às entidades reais do domínio do projeto de vocês. |
| - |

### 3.16 Tamanho e Performance

**Objetivo da seção:** registrar expectativas iniciais sobre o volume de dados, o número de usuários e os picos de utilização do sistema.

**O que vocês devem escrever:** Façam uma estimativa razoável (mesmo que aproximada) do volume de dados e de usuários esperados para o projeto de vocês, considerando o contexto do domínio escolhido.

### 3.17 Qualidade

**Objetivo da seção:** Registrar atributos de qualidade especialmente críticos para o sistema (confiabilidade, segurança, disponibilidade etc.) e explicar por que eles importam neste projeto específico.

**O que vocês devem escrever:** Identifiquem quais atributos de qualidade são mais críticos para o domínio do projeto de vocês (ex.: segurança dos dados de pagamento em um e-commerce, disponibilidade em um sistema de atendimento) e justifiquem brevemente por quê.

| **⚠ Atenção — não copiem o template** O exemplo fala em comércio exterior e ataques de hackers a um sistema financeiro — usem como referência de profundidade da análise, não como conteúdo a copiar. |
| - |

### 3.18 Cronograma Macro

**Objetivo da seção:** apresentar uma visão inicial e aproximada dos principais marcos temporais do projeto.

**O que vocês devem escrever:** Apresentem uma tabela com os marcos reais da disciplina/projeto de vocês (ex.: entrega do DVP, entrega dos requisitos, entrega do protótipo, apresentação final), indicando a semana ou a data prevista.

| **⚠ Atenção — não copiem o template** As semanas do exemplo (Semana 2, Semana 3...) são do cronograma fictício original — usem as datas/prazos reais definidos para a turma de vocês. |
| - |

### 3.19 Referências (finais)

**Objetivo da seção:** listar as fontes bibliográficas e técnicas utilizadas na elaboração do documento (padrões, normas, literatura).

**O que vocês devem escrever:** Mantenham referências realmente utilizadas, como as especificações da UML e do RUP (se aplicável ao método adotado pela disciplina), acrescentando outras fontes que a equipe efetivamente consultou.

## 4. Considerações finais

O DVP não precisa (e não deve) ser extenso por extensão; deve ser preciso. Uma seção curta, baseada em decisões reais da equipe, vale muito mais do que um parágrafo longo copiado do template só para "preencher espaço".

Antes de entregar, façam esta verificação com a equipe: se alguém apagasse o nome do projeto de todas as páginas, ainda seria possível reconhecer, pelo conteúdo, qual sistema está sendo descrito? Se a resposta for não e o texto puder servir a qualquer projeto, é sinal de que a seção precisa ser reescrita com mais especificidade.

Qualquer dúvida sobre o que escrever em uma seção específica, procurem o professor antes da entrega. É melhor esclarecer uma dúvida durante a elaboração do que perder pontos por um documento que não reflete o projeto real da equipe.
