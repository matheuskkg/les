package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;

import java.time.YearMonth;

/**
 * Cartão devolvido pelas rotas {@code /api/me/cartoes} (RF-CLI-007, DEC-07, DEC-10). O número sai mascarado ({@code
 * **** **** **** 1234}); o código de segurança nunca é devolvido nem gravado (DEC-10).
 */
public record CartaoDaResposta(
        Long id,
        String numeroMascarado,
        String nomeImpresso,
        BandeiraDaResposta bandeira,
        YearMonth validade,
        boolean preferencial) {

    public static CartaoDaResposta de(Cartao cartao) {
        return new CartaoDaResposta(
                cartao.id(),
                cartao.numeroMascarado(),
                cartao.nomeImpresso(),
                BandeiraDaResposta.de(cartao.bandeira()),
                cartao.validade(),
                cartao.preferencial());
    }
}
