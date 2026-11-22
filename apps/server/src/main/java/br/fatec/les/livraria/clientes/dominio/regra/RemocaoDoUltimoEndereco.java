package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/**
 * O último endereço não pode ser excluído (RN-CLI-001, RN-CLI-002, DEC-10). O cliente mantém ao menos um endereço, de
 * qualquer tipo, desde o cadastro; a cobrança e a entrega são escolhidas a cada compra.
 */
@Component
public class RemocaoDoUltimoEndereco implements Regra<Cliente> {

    @Override
    public void validar(Cliente alvo, Violacoes violacoes) {
        if (alvo.getEnderecos().size() <= 1) {
            violacoes.adicionar(
                    "RN-CLI-001",
                    "Não é possível excluir o único endereço do cliente; a cobrança (RN-CLI-001) e a entrega "
                            + "(RN-CLI-002) são escolhidas na compra (DEC-10).");
        }
    }
}
