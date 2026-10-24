package br.fatec.les.livraria.clientes.aplicacao.regra;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Bandeira registrada (RN-CLI-005): todo cartão do cliente deve usar uma bandeira existente na tabela do seed
 * (DEC-16). No cadastro, recebe as bandeiras já carregadas pelo caso de uso, que as reaproveita ao montar os cartões;
 * na adição e na alteração de um cartão, consulta a porta e devolve a bandeira encontrada. Um cartão sem bandeira é
 * reportado pela regra {@code CartaoCompleto}.
 */
@Component
public class BandeiraRegistrada {

    private static final String CODIGO = "RN-CLI-005";

    public void validar(NovoCliente alvo, Violacoes violacoes, Map<Long, Bandeira> bandeirasPorId) {
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            NovoCartao cartao = cartoes.get(i);
            if (cartao == null || cartao.bandeiraId() == null) {
                continue;
            }
            if (!bandeirasPorId.containsKey(cartao.bandeiraId())) {
                violacoes.adicionar(CODIGO,
                        "Cartão " + (i + 1) + ": a bandeira informada não está registrada.");
            }
        }
    }

    /**
     * Valida a bandeira de um único cartão e devolve a bandeira registrada, quando encontrada (RN-CLI-005).
     */
    public Optional<Bandeira> validar(NovoCartao cartao, Violacoes violacoes, BandeiraRepositorio bandeiras) {
        if (cartao == null || cartao.bandeiraId() == null) {
            return Optional.empty();
        }
        Optional<Bandeira> bandeira = bandeiras.buscarPorId(cartao.bandeiraId());
        if (bandeira.isEmpty()) {
            violacoes.adicionar(CODIGO, "A bandeira informada não está registrada.");
        }
        return bandeira;
    }
}
