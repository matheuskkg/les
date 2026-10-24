package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;

/** Bandeira do cartão devolvida pela API (RN-CLI-005, DEC-16). */
public record BandeiraDaResposta(Long id, String nome) {

    public static BandeiraDaResposta de(Bandeira bandeira) {
        return new BandeiraDaResposta(bandeira.id(), bandeira.nome());
    }
}
