package br.fatec.les.livraria.clientes.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeEndereco;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Escolha do endereço da compra (RF-VEN-005, DEC-10): um endereço cadastrado ou um novo, nunca os dois nem nenhum.
 * Os dados do novo são validados pelas regras do próprio endereço (RN-CLI-003).
 */
@Component
public class EscolhaDeEnderecoValida implements Regra<EscolhaDeEndereco> {

    private static final String CODIGO = "RF-VEN-005";

    @Override
    public void validar(EscolhaDeEndereco escolha, Violacoes violacoes) {
        if (escolha == null || (escolha.enderecoId() == null) == (escolha.novo() == null)) {
            violacoes.adicionar(CODIGO, "Escolha um endereço cadastrado ou informe um novo.");
        }
    }
}
