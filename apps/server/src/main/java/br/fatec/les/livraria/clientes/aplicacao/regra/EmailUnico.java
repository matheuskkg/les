package br.fatec.les.livraria.clientes.aplicacao.regra;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Email;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Unicidade do e-mail entre as contas em uso (RF-CLI-001, DEC-10). O e-mail fica no {@code Usuario}; a consulta é feita
 * pela porta pública do {@code acesso}.
 */
@Component
@RequiredArgsConstructor
public class EmailUnico implements Regra<NovoCliente> {

    private final ContasEmUso contasEmUso;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (alvo.email() == null || alvo.email().isBlank() || !Email.ehValido(alvo.email())) {
            return;
        }
        if (contasEmUso.emailEmUso(new Email(alvo.email()).endereco())) {
            violacoes.adicionar("RF-CLI-001", "Já existe uma conta em uso com este e-mail.");
        }
    }
}
