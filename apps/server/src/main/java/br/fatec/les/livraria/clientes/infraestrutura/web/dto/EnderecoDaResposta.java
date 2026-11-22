package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoLogradouro;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;

/**
 * Endereço devolvido pelas rotas {@code /api/me/enderecos} (RF-CLI-006, RNF-CLI-004, RN-CLI-003, DEC-07). O país é
 * sempre "Brasil" (DEC-10).
 */
public record EnderecoDaResposta(
        Long id,
        String nomeCurto,
        TipoEndereco tipoEndereco,
        TipoLogradouro tipoLogradouro,
        String logradouro,
        String numero,
        String bairro,
        String cep,
        String cidade,
        Uf uf,
        String pais,
        String observacoes) {

    public static EnderecoDaResposta de(Endereco endereco) {
        return new EnderecoDaResposta(
                endereco.id(),
                endereco.nomeCurto(),
                endereco.tipoEndereco(),
                endereco.tipoLogradouro(),
                endereco.logradouro(),
                endereco.numero(),
                endereco.bairro(),
                endereco.cep(),
                endereco.cidade(),
                endereco.uf(),
                endereco.pais(),
                endereco.observacoes());
    }
}
