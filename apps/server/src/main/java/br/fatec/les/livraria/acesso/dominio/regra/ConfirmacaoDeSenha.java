package br.fatec.les.livraria.acesso.dominio.regra;

import java.util.Objects;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.dominio.modelo.SenhaInformada;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/** Confirmação de senha (RNF-CLI-002): a mesma senha é digitada duas vezes no cadastro e na alteração. */
@Component
public class ConfirmacaoDeSenha implements Regra<SenhaInformada> {

    @Override
    public void validar(SenhaInformada alvo, Violacoes violacoes) {
        if (alvo.confirmacaoSenha() == null || !Objects.equals(alvo.senha(), alvo.confirmacaoSenha())) {
            violacoes.adicionar("RNF-CLI-002", "A confirmação de senha deve ser igual à senha.");
        }
    }
}
