package br.fatec.les.livraria.clientes.aplicacao.regra;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Unicidade do CPF na alteração dos dados do cliente (RF-CLI-002, DEC-10). Quando o CPF é omitido, o valor atual é
 * mantido e não há o que validar; quando informado, a própria conta é ignorada e só uma conta em uso com o mesmo CPF
 * bloqueia. Uma conta encerrada libera o CPF (DEC-10).
 */
@Component
@RequiredArgsConstructor
public class CpfUnicoNaAlteracao implements Regra<DadosAlteradosDoCliente> {

    private final ClienteRepositorio clientes;
    private final ContasEmUso contasEmUso;

    @Override
    public void validar(DadosAlteradosDoCliente alvo, Violacoes violacoes) {
        if (alvo.cpf() == null || alvo.cpf().isBlank() || !Cpf.ehValido(alvo.cpf())) {
            return;
        }
        String cpf = new Cpf(alvo.cpf()).numero();
        List<Long> outrasContas =
                clientes.buscarUsuarioIdsPorCpf(cpf).stream()
                        .filter(usuarioId -> !usuarioId.equals(alvo.usuarioId()))
                        .toList();
        if (contasEmUso.algumaEmUso(outrasContas)) {
            violacoes.adicionar("RF-CLI-002", "Já existe um cliente com este CPF em uma conta em uso.");
        }
    }
}
