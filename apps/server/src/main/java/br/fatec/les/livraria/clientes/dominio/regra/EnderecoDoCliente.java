package br.fatec.les.livraria.clientes.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Endereço do cliente (RN-CLI-001, RN-CLI-002). O DEC-10 dispensa endereços fixos de cobrança e de entrega: ao menos
 * um endereço, de qualquer tipo, atende às duas regras, e a escolha acontece a cada compra. Como a verificação é a
 * mesma, a falta de endereço gera uma única violação (o requisito RN-CLI-002 fica rastreado aqui e na mensagem).
 */
@Component
public class EnderecoDoCliente implements Regra<NovoCliente> {

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (alvo.enderecos().isEmpty()) {
            violacoes.adicionar("RN-CLI-001",
                    "Informe ao menos um endereço; a cobrança (RN-CLI-001) e a entrega (RN-CLI-002) são "
                            + "escolhidas na compra (DEC-10).");
        }
    }
}
