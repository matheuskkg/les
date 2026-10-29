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
| RNF-REC-001 | ✅ | `recomendacao/aplicacao/casodeuso/CadeiaDeProvedores.java` (falha com `WARN` no log), `recomendacao/aplicacao/regra/ProvedorEmEsperaPorCota.java`, `recomendacao/infraestrutura/externo/ConfiguracaoDosProvedoresIa.java` (um adapter por item de `livraria.ia.provedores`), `recomendacao/infraestrutura/PropriedadesIa.java` (`tool-choice`, `formato`, `cabecalhos` e `max-chamadas` por provedor), `recomendacao/infraestrutura/externo/AssistenteDeLivrosOpenAi.java` (loop de tools controlado pelo adapter, com `responder`, formatos e limites), `recomendacao/infraestrutura/externo/FerramentaDeResposta.java`, `recomendacao/dominio/regra/LimiteDeVoltasDeTools.java`, `recomendacao/dominio/regra/LimiteDeCorrecoesDeFormato.java`, `recomendacao/dominio/regra/LimiteDeChamadasHttp.java`, `recomendacao/dominio/calculo/TokensDasVoltas.java`, `recomendacao/dominio/modelo/VoltaDaChamada.java`, `recomendacao/infraestrutura/externo/AssistenteDeLivrosFake.java`, `recomendacao/infraestrutura/persistencia/` (colunas `chamadas_http` e `tokens_das_voltas` em `chamada_ia`), `seed/500_provedores_ia.sql`, `recomendacao/dominio/calculo/` (pontuação dos candidatos, filtro dos itens e validade de 24 h), `recomendacao/aplicacao/casodeuso/ConsultarMinhaRecomendacao.java`, `recomendacao/aplicacao/casodeuso/AvaliarItemRecomendado.java`, `recomendacao/infraestrutura/web/RecomendacaoController.java`, `vendas/aplicacao/casodeuso/ConsultarLivrosVendidos.java`, chat em `recomendacao/aplicacao/casodeuso/EnviarMensagemAoAssistente.java`, `recomendacao/aplicacao/FerramentasDoCatalogo.java` (tools do chat, incluindo `recomendarParaMim`), `recomendacao/dominio/regra/` (tamanho e limite por minuto da mensagem), `recomendacao/dominio/calculo/ValidarRespostaDoAssistente.java`, `recomendacao/dominio/calculo/RemoverLivrosCompradosDaRecomendacao.java` (no chat, tipo `RECOMENDACAO` sem os comprados), `recomendacao/infraestrutura/web/AssistenteController.java`, `catalogo/aplicacao/casodeuso/ConsultarSinopses.java`, admin em `recomendacao/infraestrutura/web/IaAdminController.java` (cadeia e métricas), `recomendacao/aplicacao/casodeuso/AlterarCadeiaDeProvedores.java`, `recomendacao/aplicacao/casodeuso/ConsultarMetricasDaIa.java`, `recomendacao/dominio/calculo/CalcularMetricasDosProvedores.java`; web: `modulos/catalogo/componentes/Recomendados.tsx`, `ui/BotoesDeAvaliacao.tsx`, `modulos/assistente/` (widget do chat), `modulos/admin/paginas/AdminAssistente.tsx` (aba "Assistente"); E2E: `e2e/jornadaDoAssistente.spec.ts` | O DRS pede o modelo "treinado com dados de vendas e feedback"; o fine-tuning de um modelo gratuito não é viável, e a personalização é feita pelo contexto de cada chamada ([DEC-03](../decisoes/DEC-03-recomendacao-com-ia-generativa.md)). Um provedor novo entra só por configuração (yml + seed), e cada um tem as opções `tool-choice`, `formato`, `cabecalhos` e `max-chamadas`. No chat, a recomendação usa a mesma recuperação da vitrine e não cita livros que o cliente já comprou. O loop de tools é do adapter: até 3 voltas de tools, 1 correção de formato e o total em `max-chamadas`, com `LIMITE_DE_CHAMADAS` no excesso; o formato padrão é `ferramenta` (tool `responder`), com `json-schema` e `instrucoes` como alternativas. |
