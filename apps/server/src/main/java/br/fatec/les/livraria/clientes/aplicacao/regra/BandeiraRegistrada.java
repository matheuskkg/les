package br.fatec.les.livraria.clientes.aplicacao.regra;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Bandeira registrada (RN-CLI-005): todo cartão do cliente deve usar uma bandeira existente na tabela do seed
 * (DEC-16). Um cartão sem bandeira é reportado pela regra {@code CartaoCompleto}.
 */
@Component
@RequiredArgsConstructor
public class BandeiraRegistrada implements Regra<NovoCliente> {

    private final BandeiraRepositorio bandeiras;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            NovoCartao cartao = cartoes.get(i);
            if (cartao == null || cartao.bandeiraId() == null) {
                continue;
            }
            if (bandeiras.buscarPorId(cartao.bandeiraId()).isEmpty()) {
                violacoes.adicionar("RN-CLI-005",
                        "Cartão " + (i + 1) + ": a bandeira informada não está registrada.");
            }
        }
    }
}
