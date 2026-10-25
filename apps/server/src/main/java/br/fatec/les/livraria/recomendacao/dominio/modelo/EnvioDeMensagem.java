package br.fatec.les.livraria.recomendacao.dominio.modelo;

/**
 * Mensagem que o cliente quer enviar ao assistente, com o que as regras checam antes da chamada à IA
 * (RNF-REC-001, DEC-03).
 *
 * @param texto                  texto digitado
 * @param enviadasNoUltimoMinuto mensagens do cliente gravadas no último minuto, em qualquer conversa
 */
public record EnvioDeMensagem(
        String texto,
        long enviadasNoUltimoMinuto) {
}
