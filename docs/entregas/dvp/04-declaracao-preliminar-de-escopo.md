# 4. Declaração Preliminar de Escopo

O escopo do projeto *Livraria* é descrito aqui em alto nível: o produto, o que será entregue e um resumo dos
requisitos e regras de negócio já conhecidos. O detalhamento completo fica no DRS e nos registros de decisão, e o que estiver declarado aqui deve coincidir com
as matrizes de rastreabilidade do DRS.

### 4.1. Descrição

*Livraria* é um e-commerce de livros web, de loja única (*single-tenant*), usado por dois papéis. O cliente navega
pela vitrine, monta o carrinho, paga com cartões e cupons, acompanha o pedido e solicita trocas. O administrador da
loja consulta clientes e pedidos, despacha entregas, autoriza trocas, confere o retorno dos itens ao estoque,
acompanha a análise de vendas e administra o assistente de IA.

O cliente se cadastra com senha forte e endereços, gerencia seus cartões e compra com o estoque bloqueado
temporariamente no carrinho, com prazo para finalizar. No checkout paga combinando múltiplos cartões com cupons
promocionais e de troca, e recebe recomendações e um assistente conversacional com IA generativa. O administrador
conduz o pedido pelo ciclo de status até a entrega e a eventual troca, da qual sai automaticamente um cupom de troca
para o cliente. Vitrine, pedidos, trocas, análise e assistente operam sobre os mesmos dados, com estoque verdadeiro
mantido por reservas, baixa e reentrada.

### 4.2. Produtos a serem entregues

São produtos do projeto:

| Entrega | Descrição |
| - | - |
| Sistema web | Back-end (Java 21, Spring Boot 4.1, Maven) e front-end (Preact, Vite e TypeScript, com CSS puro e formulários nativos). Em demonstração, o back-end serve o build do front-end em um único processo java -jar, em localhost:8080 (DEC-09). O artefato de entrega é o jar montado a partir do build do front-end e do empacotamento do back-end. |
| Documento de Visão de Projeto | Este documento. |
| DRS e rastreabilidade | Requisitos do produto no DRS, com matriz de rastreabilidade por grupo ligando cada requisito ao código implementado. |
| Registros de decisão (DECs) | Decisões de tecnologia e regras de negócio não definidas pelo DRS. |
| Modelo de dados | MER conceitual e lógico, conforme a decisão DEC-16 (Modelo de dados). |
| Especificações de caso de uso | Casos de uso do fluxo de Gestão de Vendas, no padrão da disciplina. |
| Plano e suíte de testes | Unitários e de caso de uso (JUnit 5), integração em H2 (@SpringBootTest), arquitetura (ArchUnit) e E2E com Playwright usando o Chrome instalado (DEC-02). |
| Seed SQL | Script de implantação que insere as tabelas de domínio, os livros, o estoque, os cupons e os dados de teste (atende RNF-LIV-002). |
| Contrato OpenAPI | Especificação gravada do back-end e tipos gerados para o front-end (DEC-07). |

### 4.3. Requisitos

Os requisitos a seguir estão resumidos por grupo do DRS; a lista completa e transcrita está no DRS, e os fluxos e as
exceções ficam no DRS e nos casos de uso.

#### 4.3.1. Requisitos Funcionais

O DRS usa o padrão "O sistema deve ...". Os grupos e os principais requisitos são:

**Cadastro de Clientes (grupo CLI do DRS)**

O sistema deve ser capaz de cadastrar, alterar, inativar e consultar clientes, registrar e consultar as transações
de um cliente, associar-lhe múltiplos endereços de entrega e cartões de crédito (com preferencial) e alterar apenas
a senha sem reeditar todo o cadastro (RF-CLI-001 a RF-CLI-008).

**Cadastro de Livros (somente leitura no MVP; grupo LIV do DRS)**

O sistema deve ser capaz de consultar livros por filtros combinados e busca livre (RF-LIV-005), apresentando a
vitrine ao visitante e ao cliente. O CRUD de livros (cadastro, inativação manual e automática, ativação, alteração;
RF-LIV-001 a RF-LIV-004 e RF-LIV-006) está fora do MVP (ver 4.3.3); os livros entram pelo seed.

**Gerenciar Vendas Eletrônicas (grupo VEN do DRS)**

O sistema deve ser capaz de gerenciar o carrinho e as quantidades (RF-VEN-001, RF-VEN-002), iniciar a compra com
cálculo de frete (RF-VEN-003, RF-VEN-004), selecionar endereço e forma de pagamento incluindo múltiplos cartões
e cupons (RF-VEN-005 a RF-VEN-007), finalizar a compra (RF-VEN-008) e conduzir o pedido à entrega
(RF-VEN-009, RF-VEN-010), além de gerenciar o ciclo de troca com cupom de troca (RF-VEN-011 a RF-VEN-015).

**Controle de Estoque (grupo EST do DRS)**

O sistema deve ser capaz de calcular o preço de venda (RF-EST-002), dar baixa no estoque das vendas aprovadas
(RF-EST-003) e reentrar itens devolvidos em troca (RF-EST-004). A entrada manual em estoque (RF-EST-001,
com as regras RN-EST-003 a RN-EST-005) está fora do MVP; o estoque e os custos vêm do seed.

**Análise (grupo ANA do DRS)**

O sistema deve ser capaz de apresentar a análise de vendas por mês e categoria em gráfico, com seleção de período
de até 24 meses e categoria, e exportar os dados em planilha CSV (RF-ANA-001 a RF-ANA-004).

**Recomendação Personalizada (grupo REC do DRS)**

O sistema deve ser capaz de recomendar livros ao cliente com IA generativa a partir do histórico de compras e das
preferências, permitir interação via chatbot para busca, dúvidas e sugestões, coletar a avaliação do cliente sobre
as respostas e registrar métricas das chamadas (RNF-REC-001, DEC-03).

**Geral (grupo GER do DRS)**

O sistema deve ser capaz de responder consultas em no máximo 1 segundo (RNF-GER-001) e registrar um log de
transações auditável (RNF-GER-002).

#### 4.3.2. Requisitos Não Funcionais

Restrições de qualidade, tecnologia e ambiente do projeto:

| Aspecto | Restrição |
| - | - |
| Stack do server | Java 21, Spring Boot 4.1.x, Maven, Spring Web, Spring Data JPA (Hibernate), Spring Security com JWT e papéis CLIENTE e ADMIN, Hibernate Envers para auditoria (RNF-GER-002), Lombok, MapStruct e springdoc (OpenAPI). Arquitetura hexagonal por módulo, testada por ArchUnit (Documento de Arquitetura do back-end). |
| Bancos de dados | PostgreSQL e H2 (em memória, modo PostgreSQL), escolhidos por profile; sem migrations, o schema é gerado pelo ORM e os dados vêm de scripts de seed executados na inicialização (DEC-14). |
| Stack do web | Preact, Vite e TypeScript, com CSS puro e formulários nativos; testes E2E com Playwright no Chrome instalado (DEC-01, DEC-02, DEC-17). |
| IA generativa | Spring AI com clientes compatíveis com OpenAI e cadeia de provedores com cota gratuita (Gemini, Groq, OpenRouter e NVIDIA NIM), além de adapter determinístico fake para testes e execução sem chave (DEC-03). |
| Segurança | Senhas codificadas com BCrypt (RNF-CLI-003), senha forte com confirmação dupla (RNF-CLI-001, RNF-CLI-002); o código de segurança (CVV) do cartão nunca é gravado. Sessão com cookie e token CSRF na mesma origem do front-end (DEC-08). |
| Valores monetários | Tipo decimal exato (BigDecimal em value object Dinheiro, escala 2 e arredondamento HALF_UP apenas ao fim do cálculo; nunca o tipo double) (DEC-15.1). |
| Tempo de resposta | Consultas de usuário em no máximo 1 segundo, com paginação e índices declarados (RNF-GER-001). |
| Log de transação | Auditoria de dados alterados com Envers (entidades anotadas com @Audited), com revisão que inclui o usuário e a data/hora (RNF-GER-002). |
| Navegador e plataforma | Aplicação web executada no Chrome; ambiente local de desenvolvimento (Linux, Java 21, Node/Vite, Docker Compose para o PostgreSQL). |

#### 4.3.3. Regras de Negócio

As regras completas estão no DRS; as principais, em resumo:

Cada cliente tem ao menos um endereço, sem papel fixo de entrega ou cobrança, e o cadastro exige os dados
obrigatórios; o endereço e o cartão têm composição obrigatória, a bandeira registrada é identificada pelo
número do cartão e o ranking varia conforme o perfil de compra (RN-CLI-001 a RN-CLI-007; DEC-04, DEC-10 e DEC-18).

Só entra no carrinho o que há de disponível (RN-VEN-001). O carrinho fica bloqueado por prazo parametrizado, com
aviso antes de expirar e retirada de todos os itens do mesmo produto ao expirar (RN-VEN-014 e RN-VEN-015,
DEC-05.1); itens removidos ficam visíveis no carrinho (RNF-VEN-001).

A compra aceita um cupom promocional por pedido (RN-VEN-003, DEC-15.2) e distribui o valor entre múltiplos cartões,
cada um com valor mínimo (RN-VEN-004), com exceção quando os cupons cobrem quase todo o total (RN-VEN-005); cupons
que superam a compra geram cupom de troca, sem emitir cupons desnecessários (RN-VEN-006, DEC-11). A validação na
operadora simulada (RN-VEN-007, DEC-05.2) define o pedido como APROVADA ou REPROVADA (RN-VEN-008).

As transições de status do pedido acontecem apenas na ordem definida em DEC-12 (RN-VEN-009, RN-VEN-010). A troca
tem ciclo próprio: aberta EM TROCA, passa a TROCA AUTORIZADA ou TROCA RECUSADA e, autorizada, termina em
TROCADO
(RN-VEN-011, RN-VEN-012), só após pedido ENTREGUE e dentro do prazo (RN-VEN-013), com notificação ao cliente
(RN-VEN-016). O pedido é gravado EM PROCESSAMENTO e a validação do pagamento e as notificações são processadas em
seguida (DEC-13).

O preço de venda resulta do maior custo entre as entradas multiplicado pela margem do grupo de precificação
(RF-EST-002, RN-EST-002); a baixa acontece apenas nas compras aprovadas (RN-EST-006), o saldo disponível desconta
as reservas (DEC-15.3) e os itens de troca reentram no estoque quando marcados como volta ao estoque (RF-EST-004).

A análise agrega as vendas por mês e categoria a partir do snapshot do pedido, com período de até 24 meses
(RN-ANA-001 a RN-ANA-004); a exportação é feita apenas em CSV (DEC-15.4).

Ficam fora do MVP o CRUD de livros (RF-LIV-001 a RF-LIV-004 e RF-LIV-006, com as regras RN-LIV-001, RN-LIV-002 e
RN-LIV-004 a RN-LIV-007) e a entrada manual de estoque (RF-EST-001, RN-EST-003 a RN-EST-005). Livros,
estoque e tabelas de domínio (autor, editora, categoria, grupo de precificação, fornecedor, bandeiras de cartão)
são inseridos pelo script SQL de seed, que atende RNF-LIV-002. As regras de cálculo de preço e as interações de
leitura continuam valendo sobre os dados do seed.

**Cronograma de Marcos Sumariado**

Os marcos definidos pela disciplina para 2026:

| Data | Resultado |
| - | - |
| 18/08 | Apresentação do projeto |
| 12/09 | Versão inicial do DVP |
| 22/09 | CRUD completo de clientes |
| 29/09 | Especificação de caso de uso do fluxo de Gestão de Vendas |
| 06/10 | Fluxo de criação de pedido |
| 20/10 | Implementação do UC de Gestão de Vendas |
| 10/11 | Módulo de análise |
| 24/11 | Módulo de recomendações |

Estes marcos são a referência dos prazos e valem também como premissa (seção 5); a seção 15 os detalha.
