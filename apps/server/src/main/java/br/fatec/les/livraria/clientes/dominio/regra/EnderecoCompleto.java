package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/**
 * Composição do endereço (RN-CLI-003): nome curto, tipo de endereço, tipo de logradouro, logradouro, número, bairro,
 * CEP, cidade e estado. O país é sempre "Brasil" e o CEP tem 8 dígitos (DEC-10). Valida um endereço; a lista do
 * cadastro é percorrida por {@code EnderecosCompletos}.
 */
@Component
public class EnderecoCompleto implements Regra<NovoEndereco> {

    private static final String CODIGO = "RN-CLI-003";

    @Override
    public void validar(NovoEndereco endereco, Violacoes violacoes) {
        if (endereco == null) {
            violacoes.adicionar(CODIGO, "Informe os dados do endereço.");
            return;
        }
        if (emBranco(endereco.nomeCurto())) {
            violacoes.adicionar(CODIGO, "Informe o nome curto.");
        }
        if (endereco.tipoEndereco() == null) {
            violacoes.adicionar(CODIGO, "Informe o tipo de endereço.");
        }
        if (endereco.tipoLogradouro() == null) {
            violacoes.adicionar(CODIGO, "Informe o tipo de logradouro.");
        }
        if (emBranco(endereco.logradouro())) {
            violacoes.adicionar(CODIGO, "Informe o logradouro.");
        }
        if (emBranco(endereco.numero())) {
            violacoes.adicionar(CODIGO, "Informe o número.");
        }
        if (emBranco(endereco.bairro())) {
            violacoes.adicionar(CODIGO, "Informe o bairro.");
        }
        if (!cepValido(endereco.cep())) {
            violacoes.adicionar(CODIGO, "Informe um CEP com 8 dígitos.");
        }
        if (emBranco(endereco.cidade())) {
            violacoes.adicionar(CODIGO, "Informe a cidade.");
        }
        if (endereco.uf() == null) {
            violacoes.adicionar(CODIGO, "Informe o estado (UF).");
        }
    }

    private static boolean cepValido(String cep) {
        return cep != null && Telefone.somenteDigitos(cep).length() == 8;
    }

    private static boolean emBranco(String valor) {
        return valor == null || valor.isBlank();
    }
}
