package br.fatec.les.livraria.clientes.aplicacao.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Unicidade do telefone entre as contas em uso (RF-CLI-001, DEC-10): a comparação é por DDD + número, sem considerar o
 * tipo. O telefone fica no {@code Usuario}; a consulta é feita pela porta pública do {@code acesso}.
 */
@Component
@RequiredArgsConstructor
public class TelefoneUnico implements Regra<NovoCliente> {

    private final ContasEmUso contasEmUso;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (!telefoneValido(alvo.ddd(), alvo.numero())) {
            return;
        }
        if (contasEmUso.telefoneEmUso(somenteDigitos(alvo.ddd()), somenteDigitos(alvo.numero()))) {
            violacoes.adicionar("RF-CLI-001", "Já existe uma conta em uso com este telefone.");
        }
    }

    private static boolean telefoneValido(String ddd, String numero) {
        if (ddd == null || numero == null) {
            return false;
        }
        int digitosDoNumero = somenteDigitos(numero).length();
        return somenteDigitos(ddd).length() == 2 && (digitosDoNumero == 8 || digitosDoNumero == 9);
    }

    private static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
