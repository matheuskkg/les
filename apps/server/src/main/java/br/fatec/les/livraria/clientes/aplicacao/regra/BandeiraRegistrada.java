package br.fatec.les.livraria.clientes.aplicacao.regra;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Bandeira registrada (RN-CLI-005): todo cartão do cliente deve usar uma bandeira existente na tabela do seed
 * (DEC-16). Recebe as bandeiras já carregadas pelo caso de uso, que as reaproveita ao montar os cartões. Um cartão sem
 * bandeira é reportado pela regra {@code CartaoCompleto}.
 */
@Component
public class BandeiraRegistrada {

    public void validar(NovoCliente alvo, Violacoes violacoes, Map<Long, Bandeira> bandeirasPorId) {
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            NovoCartao cartao = cartoes.get(i);
            if (cartao == null || cartao.bandeiraId() == null) {
                continue;
            }
            if (!bandeirasPorId.containsKey(cartao.bandeiraId())) {
                violacoes.adicionar("RN-CLI-005",
                        "Cartão " + (i + 1) + ": a bandeira informada não está registrada.");
            }
        }
    }
}
