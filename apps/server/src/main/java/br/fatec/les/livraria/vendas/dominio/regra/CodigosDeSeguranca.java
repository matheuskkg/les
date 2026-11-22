package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cada cartão da finalização traz o código de segurança, com 3 ou 4 dígitos (RN-CLI-004, DEC-10). O código é pedido a
 * cada compra e só segue para a operadora.
 */
@Component
public class CodigosDeSeguranca implements Regra<List<String>> {

    @Override
    public void validar(List<String> codigos, Violacoes violacoes) {
        for (int i = 0; i < codigos.size(); i++) {
            String codigo = codigos.get(i);
            if (codigo == null || !codigo.matches("\\d{3,4}")) {
                violacoes.adicionar(
                        "RN-CLI-004", "Informe o código de segurança de 3 ou 4 dígitos do cartão " + (i + 1) + ".");
            }
        }
    }
}
