package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;

/**
 * Reenvio da última mensagem que ficou sem resposta (RNF-REC-001, DEC-03): quando toda a cadeia falha, a mensagem do
 * cliente fica gravada; o "tentar de novo" manda o mesmo texto, que não é gravado outra vez nem entra duas vezes no
 * histórico enviado à IA.
 */
@Component
public class ReenvioSemResposta {

    /**
     * @param ultimas mensagens da conversa ativa, da mais antiga à mais recente
     * @param texto   texto enviado agora, sem espaços nas pontas
     */
    public boolean reenvio(List<MensagemDaConversa> ultimas, String texto) {
        if (ultimas.isEmpty()) {
            return false;
        }
        MensagemDaConversa ultima = ultimas.getLast();
        return ultima.papel() == PapelDaMensagem.CLIENTE && ultima.texto().equals(texto);
    }
}
