package br.fatec.les.livraria.clientes.aplicacao.regra;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Unicidade do telefone entre as contas em uso (RF-CLI-001, DEC-10): a comparação é por DDD + número, sem considerar o
 * tipo. O telefone fica no {@code Usuario}; a consulta é feita pela porta pública do {@code acesso}. O formato é
 * verificado por {@link Telefone}, o mesmo helper de {@code DadosObrigatoriosCliente}.
 */
@Component
@RequiredArgsConstructor
public class TelefoneUnico implements Regra<NovoCliente> {

    private final ContasEmUso contasEmUso;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (!Telefone.valido(alvo.ddd(), alvo.numero())) {
            return;
        }
        if (contasEmUso.telefoneEmUso(Telefone.somenteDigitos(alvo.ddd()), Telefone.somenteDigitos(alvo.numero()))) {
            violacoes.adicionar("RF-CLI-001", "Já existe uma conta em uso com este telefone.");
        }
    }
}
