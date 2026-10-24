package br.fatec.les.livraria.clientes.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Endereço de cobrança (RN-CLI-001). O DEC-10 dispensa um endereço de cobrança fixo: ao menos um endereço já atende
 * a regra, e a cobrança é escolhida a cada compra.
 */
@Component
public class EnderecoDeCobranca implements Regra<NovoCliente> {

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (alvo.enderecos().isEmpty()) {
            violacoes.adicionar("RN-CLI-001",
                    "Informe ao menos um endereço; a cobrança é escolhida na compra (DEC-10).");
        }
    }
}
