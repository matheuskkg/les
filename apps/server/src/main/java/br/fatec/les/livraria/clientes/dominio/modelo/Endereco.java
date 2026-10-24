package br.fatec.les.livraria.clientes.dominio.modelo;

import java.util.Objects;

/**
 * Endereço do cliente (RN-CLI-003, RF-CLI-006, DEC-10). Não tem papel: entrega e cobrança são escolhidas na compra.
 * O país é sempre o Brasil e o CEP é guardado com os 8 dígitos.
 */
public record Endereco(
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

    /** País único aceito no cadastro (DEC-10). */
    public static final String PAIS = "Brasil";

    public Endereco {
        Objects.requireNonNull(nomeCurto, "nomeCurto");
        Objects.requireNonNull(tipoEndereco, "tipoEndereco");
        Objects.requireNonNull(tipoLogradouro, "tipoLogradouro");
        Objects.requireNonNull(logradouro, "logradouro");
        Objects.requireNonNull(numero, "numero");
        Objects.requireNonNull(bairro, "bairro");
        Objects.requireNonNull(cep, "cep");
        Objects.requireNonNull(cidade, "cidade");
        Objects.requireNonNull(uf, "uf");
        Objects.requireNonNull(pais, "pais");
    }
}
