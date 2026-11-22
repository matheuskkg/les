package br.fatec.les.livraria.clientes.aplicacao.regra;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Email;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Unicidade do e-mail na alteração dos dados do cliente (RF-CLI-002, DEC-10). A própria conta é ignorada: manter o
 * e-mail atual não bloqueia a operação, mas o e-mail de outra conta em uso é uma violação.
 */
@Component
@RequiredArgsConstructor
public class EmailUnicoNaAlteracao implements Regra<DadosAlteradosDoCliente> {

    private final ContasEmUso contasEmUso;

    @Override
    public void validar(DadosAlteradosDoCliente alvo, Violacoes violacoes) {
        if (alvo.email() == null || alvo.email().isBlank() || !Email.ehValido(alvo.email())) {
            return;
        }
        if (contasEmUso.emailEmUsoExceto(new Email(alvo.email()).endereco(), alvo.usuarioId())) {
            violacoes.adicionar("RF-CLI-002", "Já existe uma conta em uso com este e-mail.");
        }
    }
}
