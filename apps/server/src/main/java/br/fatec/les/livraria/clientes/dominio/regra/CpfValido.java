package br.fatec.les.livraria.clientes.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.DadosCadastraisCliente;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * CPF válido (RN-CLI-006), com os dígitos verificadores conferidos pelo value object {@link Cpf}. Na alteração o CPF
 * pode ser omitido; quando informado, tem o mesmo formato exigido no cadastro.
 */
@Component
public class CpfValido implements Regra<DadosCadastraisCliente> {

    @Override
    public void validar(DadosCadastraisCliente alvo, Violacoes violacoes) {
        if (alvo.cpf() != null && !alvo.cpf().isBlank() && !Cpf.ehValido(alvo.cpf())) {
            violacoes.adicionar("RN-CLI-006", "CPF inválido.");
        }
    }
}
