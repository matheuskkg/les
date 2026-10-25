# Recomendação Personalizada

Prefixo do grupo: `REC`. Fonte: DRS – E-commerce de Livros (LES 2026), v0.6.

## Requisitos funcionais

_Nenhum neste grupo._

## Requisitos não funcionais

| ID | Nome | Descrição |
|---|---|---|
| RNF-REC-001 | Recomendação com IA generativa | Integrar IA generativa para recomendações personalizadas de livros com base no histórico de compras e preferências. Permitir interação via chatbot para busca, dúvidas e sugestões. O modelo deve ser treinado com dados de vendas e feedback dos usuários, garantindo personalização contínua. |

## Regras de negócio

_Nenhum neste grupo._

## Matriz de rastreabilidade

Status: ✅ implementado · 🟡 parcial · ❌ pendente.

| ID | Status | Onde no código | Observação |
|---|---|---|---|
| RNF-REC-001 | 🟡 | `recomendacao/aplicacao/casodeuso/CadeiaDeProvedores.java`, `recomendacao/aplicacao/regra/ProvedorEmEsperaPorCota.java`, `recomendacao/infraestrutura/externo/AssistenteDeLivrosOpenAi.java`, `recomendacao/infraestrutura/externo/AssistenteDeLivrosFake.java`, `recomendacao/infraestrutura/persistencia/`, `seed/500_provedores_ia.sql`, `recomendacao/dominio/calculo/` (pontuação dos candidatos, filtro dos itens e validade de 24 h), `recomendacao/aplicacao/casodeuso/ConsultarMinhaRecomendacao.java`, `recomendacao/aplicacao/casodeuso/AvaliarItemRecomendado.java`, `recomendacao/infraestrutura/web/RecomendacaoController.java`, `vendas/aplicacao/casodeuso/ConsultarLivrosVendidos.java`; web: `modulos/catalogo/componentes/Recomendados.tsx`, `ui/BotoesDeAvaliacao.tsx` | O DRS pede o modelo "treinado com dados de vendas e feedback"; o fine-tuning de um modelo gratuito não é viável, e a personalização é feita pelo contexto de cada chamada ([DEC-03](../decisoes/DEC-03-recomendacao-com-ia-generativa.md)). |
