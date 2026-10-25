package br.fatec.les.livraria.recomendacao.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.dominio.modelo.EnvioDeMensagem;

/**
 * O cliente envia no máximo {@value #LIMITE} mensagens por minuto ao assistente (RNF-REC-001, DEC-03). A contagem
 * vem de {@code mensagem_conversa} e inclui as conversas arquivadas, para "Limpar conversa" não zerar o limite.
 */
@Component
public class LimiteDeMensagensPorMinuto implements Regra<EnvioDeMensagem> {

    public static final int LIMITE = 10;
    private static final String CODIGO = "RNF-REC-001";

    @Override
    public void validar(EnvioDeMensagem alvo, Violacoes violacoes) {
        if (alvo.enviadasNoUltimoMinuto() >= LIMITE) {
            violacoes.adicionar(CODIGO, "Você enviou muitas mensagens. Aguarde um minuto e tente de novo.");
        }
    }
}
