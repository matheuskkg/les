package br.fatec.les.livraria.clientes.dominio.modelo;

import java.util.Objects;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;

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

    /**
     * Monta o endereço a partir dos dados informados (RN-CLI-003, DEC-10), normalizando o CEP para 8 dígitos e
     * aparando os textos. O país não vem da requisição: é sempre "Brasil".
     */
    public static Endereco de(NovoEndereco dados, Long id) {
        return new Endereco(id, dados.nomeCurto().strip(), dados.tipoEndereco(), dados.tipoLogradouro(),
                dados.logradouro().strip(), dados.numero().strip(), dados.bairro().strip(),
                Telefone.somenteDigitos(dados.cep()), dados.cidade().strip(), dados.uf(), PAIS,
                textoOuNulo(dados.observacoes()));
    }

    private static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
