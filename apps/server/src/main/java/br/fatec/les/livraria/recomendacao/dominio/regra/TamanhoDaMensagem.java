package br.fatec.les.livraria.recomendacao.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.dominio.modelo.EnvioDeMensagem;

/**
 * A mensagem ao assistente não pode ficar em branco e tem no máximo {@value #TAMANHO_MAXIMO} caracteres
 * (RNF-REC-001, DEC-03). Os caracteres são contados por code point, como o contador do widget.
 */
@Component
public class TamanhoDaMensagem implements Regra<EnvioDeMensagem> {

    public static final int TAMANHO_MAXIMO = 500;
    private static final String CODIGO = "RNF-REC-001";

    @Override
    public void validar(EnvioDeMensagem alvo, Violacoes violacoes) {
        String texto = alvo.texto() == null ? "" : alvo.texto().strip();
        if (texto.isEmpty()) {
            violacoes.adicionar(CODIGO, "Escreva a mensagem.");
        } else if (texto.codePointCount(0, texto.length()) > TAMANHO_MAXIMO) {
            violacoes.adicionar(CODIGO, "A mensagem pode ter até " + TAMANHO_MAXIMO + " caracteres.");
        }
    }
}
