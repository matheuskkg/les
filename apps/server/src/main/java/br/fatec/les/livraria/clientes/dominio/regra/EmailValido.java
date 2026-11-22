package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.clientes.dominio.modelo.DadosCadastraisCliente;
import br.fatec.les.livraria.compartilhado.dominio.Email;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/** E-mail com formato válido (RN-CLI-006, DEC-10), conferido pelo value object {@link Email}. */
@Component
public class EmailValido implements Regra<DadosCadastraisCliente> {

    @Override
    public void validar(DadosCadastraisCliente alvo, Violacoes violacoes) {
        if (alvo.email() != null && !alvo.email().isBlank() && !Email.ehValido(alvo.email())) {
            violacoes.adicionar("RN-CLI-006", "E-mail inválido.");
        }
    }
}
