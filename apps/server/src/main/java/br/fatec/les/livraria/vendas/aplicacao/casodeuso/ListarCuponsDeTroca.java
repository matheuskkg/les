package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsDeTroca;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;
import lombok.RequiredArgsConstructor;

/**
 * Cupons de troca disponíveis do cliente autenticado, para a escolha no checkout (RF-VEN-006, RF-VEN-007, DEC-11):
 * não usados e não vencidos, dos mais antigos aos mais novos.
 */
@Component
@RequiredArgsConstructor
public class ListarCuponsDeTroca {

    private final IdentificarCliente identificarCliente;
    private final CuponsDeTroca cuponsDeTroca;
    private final Relogio relogio;

    @Transactional(readOnly = true)
    public List<CupomTroca> executar() {
        return cuponsDeTroca.listarNaoUsados(identificarCliente.executar()).stream()
                .filter(cupom -> cupom.disponivelEm(relogio.hoje()))
                .toList();
    }
}
