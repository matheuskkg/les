# 1. Objetivo

Este documento formaliza o início do projeto *Livraria: e-commerce de livros*, desenvolvido na disciplina de
Laboratório de Engenharia de Software (LES) da FATEC em 2026. Ele registra as necessidades de negócio que motivam a
solução, o objetivo do projeto, uma declaração preliminar de escopo, as premissas assumidas e a influência das
partes interessadas, de modo a definir o problema a ser resolvido, para quem e dentro de quais limites. Serve de
base para os demais artefatos do projeto: os requisitos detalhados (DRS), as decisões registradas (DECs), o modelo
de dados, a arquitetura e o planejamento dos marcos da disciplina.

O produto é um e-commerce de livros *single-tenant* (uma única loja): clientes compram livros, pagam com múltiplos
cartões e cupons, acompanham o pedido até a entrega e podem trocar itens. O administrador da loja opera pedidos,
trocas, análise de vendas e o assistente de recomendação com IA generativa. A especificação completa do produto está
no Documento de Requisitos de Software (DRS v0.6).

### 1.1. Escopo

Este documento cobre o projeto em alto nível: as necessidades de negócio (seção 2), o objetivo e a declaração
preliminar de escopo (seções 3 e 4), as premissas e as partes interessadas (seções 5 e 6), a estrutura técnica em
visões arquiteturais (seções 7 a 12), as expectativas de tamanho e qualidade (seções 13 e 14) e o cronograma macro
(seção 15). O detalhamento dos requisitos fica no DRS, grupo por grupo, com as matrizes de rastreabilidade para o código; as regras que o DRS não
define ficam nos Registros de Decisão do projeto (DECs); a organização técnica está no Documento de Arquitetura do
back-end e o modelo de dados, no Modelo de Dados (MER conceitual e lógico).

### 1.2. Referências

Para a construção deste documento foram utilizadas as seguintes referências:

- Guia de Elaboração do DVP e template de DVP, material da disciplina (LES, FATEC, Prof. Rodrigo Rocha Silva),
  incluindo as orientações de preenchimento, a estrutura deste documento e os prazos e marcos do semestre.
- Documento de Requisitos de Software (DRS) v0.6, *E-commerce de Livros*, com um grupo por área: Geral (GER),
  Livros (LIV), Clientes (CLI), Vendas (VEN), Estoque (EST), Análise (ANA) e Recomendação (REC).
- Registros de Decisão do projeto, no estilo MADR, mantidos com o código no repositório GitHub do
  projeto (github.com/matheuskkg/les):
  - DEC-01: Stack do web
  - DEC-02: Framework de testes E2E
  - DEC-03: Recomendação e chatbot com IA generativa
  - DEC-04: Ranking de cliente
  - DEC-05.1: Prazo de bloqueio do carrinho
  - DEC-05.2: Operadora de cartão simulada
  - DEC-05.3: Cálculo de frete simulado
  - DEC-06: Data de validade no cartão de crédito
  - DEC-07: Convenções da API REST
  - DEC-08: Autenticação, sessão e modelo de usuário
  - DEC-09: Topologia de execução de web e server
  - DEC-10: Modelo de cliente, endereços e cartões
  - DEC-11: Cupons e distribuição do pagamento
  - DEC-12: Pedido, checkout e trocas
  - DEC-13: Processamento do pedido e notificações
  - DEC-14: IDs, nomes e seed
  - DEC-15.1: Arredondamento
  - DEC-15.2: Uso do cupom promocional
  - DEC-15.3: Saldo de estoque
  - DEC-15.4: Ajustes de acesso, consultas e exportação
  - DEC-16: Modelo de dados
  - DEC-17: Organização do código do web
  - DEC-18: Bandeira do cartão identificada pelo número
- Documento de Arquitetura do back-end.
- Modelo de Dados (MER conceitual e lógico).
- Guia de Configuração da IA.
- Definição de escopo do MVP do projeto.

Este documento influencia a evolução dos seguintes artefatos: o DRS e suas matrizes de rastreabilidade (requisitos),
os registros de decisão, o modelo de dados e a arquitetura, o plano de testes (unitários, de integração e E2E do
repositório) e o planejamento e cronograma das entregas da disciplina (seção 4.3.3). Mudanças de escopo registradas
aqui devem refletir-se neles.
