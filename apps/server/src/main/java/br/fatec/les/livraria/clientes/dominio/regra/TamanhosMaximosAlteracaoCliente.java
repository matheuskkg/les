package br.fatec.les.livraria.clientes.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.DadosCadastraisCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Tamanho máximo do nome na alteração dos dados do cliente (RF-CLI-002, RN-CLI-006). A constante é a mesma da coluna
 * {@code cliente.nome}; sem esta regra, um nome maior chegaria ao banco e viraria erro 500 em vez de violação 422.
 */
@Component
public class TamanhosMaximosAlteracaoCliente implements Regra<DadosCadastraisCliente> {

    private static final String CODIGO = "RN-CLI-006";

    @Override
    public void validar(DadosCadastraisCliente alvo, Violacoes violacoes) {
        if (alvo.nome() != null && alvo.nome().strip().length() > TamanhosMaximosCliente.NOME) {
            violacoes.adicionar(CODIGO,
                    "O nome do cliente deve ter no máximo " + TamanhosMaximosCliente.NOME + " caracteres.");
        }
    }
}
