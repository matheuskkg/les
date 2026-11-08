# 15. Cronograma Macro

O cronograma segue os oito marcos definidos pela disciplina para 2026. Cada marco reúne um ou mais grupos do DRS
e termina com um resultado que pode ser conferido: uma apresentação, um documento, um conjunto de telas e
regras funcionando ou um módulo com seus testes. A ordem reflete a dependência entre as partes, já que a venda
usa o cadastro de clientes e o estoque, e a análise e as recomendações leem os pedidos gravados.

| Data | Marco | Entregáveis principais |
| - | - | - |
| 18/08 | Apresentação do projeto | Apresentação do escopo do produto, dos papéis de cliente e administrador e da arquitetura planejada, com a visão geral dos grupos do DRS. |
| 12/09 | Versão inicial do DVP | Versão inicial do Documento de Visão de Projeto, com o escopo declarado e os requisitos resumidos por grupo do DRS. |
| 22/09 | CRUD completo de clientes | Grupo Clientes: cadastro, alteração, inativação e consulta de clientes, com endereços, cartões e troca de senha (RF-CLI-001 a RF-CLI-008), e senha protegida por BCrypt. |
| 29/09 | Especificação de caso de uso do fluxo de Gestão de Vendas | Especificação do caso de uso de Gestão de Vendas no padrão da disciplina, com os atores, o fluxo principal e as exceções, alinhada ao DRS do grupo Vendas. |
| 06/10 | Fluxo de criação de pedido | Grupos Vendas e Estoque: carrinho com reserva de estoque e bloqueio por prazo, cálculo de frete, escolha de endereço e de pagamento com múltiplos cartões e cupons (RF-VEN-001 a RF-VEN-008), e gravação do pedido como EM PROCESSAMENTO. |
| 20/10 | Implementação do UC de Gestão de Vendas | Grupos Vendas e Estoque: validação do pagamento na operadora simulada, andamento do pedido até ENTREGUE, trocas com cupom de troca (RF-VEN-009 a RF-VEN-015), baixa e reentrada de estoque (RF-EST-003 e RF-EST-004). |
| 10/11 | Módulo de análise | Grupo Análise: gráfico de vendas por mês e categoria, com seleção de período de até 24 meses e exportação em CSV (RF-ANA-001 a RF-ANA-004). |
| 24/11 | Módulo de recomendações | Grupo Recomendação: recomendação personalizada e chatbot com IA generativa, coleta da avaliação das respostas e registro das chamadas de IA (RNF-REC-001). |

As datas seguem o calendário da disciplina e podem ser ajustadas pelo professor.
