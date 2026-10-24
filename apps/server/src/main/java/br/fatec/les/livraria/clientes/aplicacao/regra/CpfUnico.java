package br.fatec.les.livraria.clientes.aplicacao.regra;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Unicidade do CPF entre as contas em uso (RF-CLI-001, DEC-10). Busca os clientes com o CPF e pergunta ao
 * {@code acesso} se o usuário de algum ainda está ativo ou dentro do prazo de reativação; conta encerrada libera o
 * CPF para um novo cadastro.
 */
@Component
@RequiredArgsConstructor
public class CpfUnico implements Regra<NovoCliente> {

    private final ClienteRepositorio clientes;
    private final ContasEmUso contasEmUso;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (alvo.cpf() == null || alvo.cpf().isBlank() || !Cpf.ehValido(alvo.cpf())) {
            return;
        }
        String cpf = new Cpf(alvo.cpf()).numero();
        List<Long> usuarioIds = clientes.buscarUsuarioIdsPorCpf(cpf);
        if (contasEmUso.algumaEmUso(usuarioIds)) {
            violacoes.adicionar("RF-CLI-001", "Já existe um cliente com este CPF em uma conta em uso.");
        }
    }
}
