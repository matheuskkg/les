# 6. Influência das Partes Interessadas

A orientação e a avaliação da disciplina, o único responsável pelo desenvolvimento, os papéis de usuário do sistema
e as dependências externas de IA têm influência direta sobre os objetivos, o escopo e o ritmo do projeto.

| Interessado | Papel no projeto | Interesse específico | Influência |
| - | - | - | - |
| Rodrigo Rocha Silva (professor, FATEC/LES) | Orientador e avaliador | Verificar aderência do produto aos requisitos do DRS, à arquitetura acordada e aos prazos e marcos da disciplina (seção 4.3.3); orientar decisões e revisar este DVP. | Alta: define o escopo mínimo aceito (MVP), o calendário das entregas e o critério de aprovação; o feedback nas revisões altera prioridades e a versão do DVP. |
| Matheus Kazuo Koike Graciliano | Único desenvolvedor e analista | Entregar o MVP completo com qualidade testável, mantendo as matrizes de rastreabilidade e as decisões atualizadas. | Alta: decide a implementação técnica dia a dia; a capacidade individual é o principal limite do cronograma (premissa da seção 5). |
| Cliente da loja (papel de usuário do sistema) | Ator do e-commerce | Comprar livros com carrinho, pagamento por múltiplos cartões e cupons, acompanhar o pedido, ser notificado e trocar itens recebendo cupom de troca. | Indireta: é o papel simulado em sala pelo próprio professor para validar os fluxos de vendas e de clientes (VEN, CLI); requisitos de usabilidade e notificações dependem dessa perspectiva. |
| Administrador da loja (papel de usuário do sistema) | Ator operacional | Operar pedidos (despachar, confirmar entrega), autorizar ou recusar trocas, conferir a análise de vendas em gráfico e CSV e administrar os provedores de IA. | Indireta: é o papel do operador dos fluxos de negócio e da análise (RF-VEN-009, RF-VEN-010, RF-VEN-012 a RF-VEN-015 e RF-ANA-001 a RF-ANA-004); define se o sistema permite operar a loja sem planilhas paralelas. |
| Provedores externos de IA (Gemini, Groq, OpenRouter e NVIDIA NIM) | Dependência técnica do assistente (RNF-REC-001, DEC-03) | Disponibilizar modelos compatíveis com OpenAI com cota gratuita, com latência e limites de uso variáveis. | Moderada: podem degradar ou esgotar a cota do assistente; a cadeia de provedores com espera por cota, a gravação de métricas e o adapter determinístico fake reduzem o impacto na demonstração e nos testes sem rede. |

