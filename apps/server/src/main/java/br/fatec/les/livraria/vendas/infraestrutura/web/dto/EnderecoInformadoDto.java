package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoLogradouro;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;

/** Endereço novo informado na compra (RF-VEN-005, RN-CLI-003, DEC-10). O país não é editável: é sempre "Brasil". */
public record EnderecoInformadoDto(
        String nomeCurto,
        TipoEndereco tipoEndereco,
        TipoLogradouro tipoLogradouro,
        String logradouro,
        String numero,
        String bairro,
        String cep,
        String cidade,
        Uf uf,
        String observacoes) {

    public NovoEndereco paraNovoEndereco() {
        return new NovoEndereco(nomeCurto, tipoEndereco, tipoLogradouro, logradouro, numero, bairro, cep, cidade, uf,
                observacoes);
    }
}
