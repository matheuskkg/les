package br.fatec.les.livraria.clientes.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Endereço de entrega (RN-CLI-002). O DEC-10 dispensa um endereço de entrega fixo: ao menos um endereço já atende a
 * regra, e a entrega é escolhida a cada compra.
 */
@Component
public class EnderecoDeEntrega implements Regra<NovoCliente> {

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (alvo.enderecos().isEmpty()) {
            violacoes.adicionar("RN-CLI-002",
                    "Informe ao menos um endereço; a entrega é escolhida na compra (DEC-10).");
        }
    }
}
