package br.fatec.les.livraria.clientes.dominio.calculo;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;

/**
 * Tamanho do número conforme a bandeira identificada (RN-CLI-004, DEC-18): a coluna {@code tamanhos} da bandeira lista
 * os comprimentos aceitos, por exemplo 13, 16 e 19 para a Visa.
 */
@Component
public class TamanhoDoCartao {

    /** {@code true} se o número, com ou sem máscara, tem um dos comprimentos aceitos pela bandeira. */
    public boolean aceita(String numero, Bandeira bandeira) {
        if (numero == null || bandeira == null) {
            return false;
        }
        int tamanho = Telefone.somenteDigitos(numero).length();
        return bandeira.tamanhos().contains(tamanho);
    }
}
