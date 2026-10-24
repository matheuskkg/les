package br.fatec.les.livraria.clientes.dominio.regra;

import java.util.Objects;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/** Confirmação de senha (RNF-CLI-002): o cliente digita a mesma senha duas vezes no cadastro. */
@Component
public class ConfirmacaoDeSenha implements Regra<NovoCliente> {

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (alvo.confirmacaoSenha() == null || !Objects.equals(alvo.senha(), alvo.confirmacaoSenha())) {
            violacoes.adicionar("RNF-CLI-002", "A confirmação de senha deve ser igual à senha.");
        }
    }
}
