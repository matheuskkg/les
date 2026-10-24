package br.fatec.les.livraria.clientes.dominio.regra;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Composição do endereço (RN-CLI-003): nome curto, tipo de endereço, tipo de logradouro, logradouro, número, bairro,
 * CEP, cidade e estado. O país é sempre "Brasil" e o CEP tem 8 dígitos (DEC-10).
 */
@Component
public class EnderecoCompleto implements Regra<NovoCliente> {

    private static final String CODIGO = "RN-CLI-003";

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        List<NovoEndereco> enderecos = alvo.enderecos();
        for (int i = 0; i < enderecos.size(); i++) {
            NovoEndereco endereco = enderecos.get(i);
            String referencia = "Endereço " + (i + 1) + ": ";
            if (endereco == null) {
                violacoes.adicionar(CODIGO, referencia + "informe os dados do endereço.");
                continue;
            }
            if (emBranco(endereco.nomeCurto())) {
                violacoes.adicionar(CODIGO, referencia + "informe o nome curto.");
            }
            if (endereco.tipoEndereco() == null) {
                violacoes.adicionar(CODIGO, referencia + "informe o tipo de endereço.");
            }
            if (endereco.tipoLogradouro() == null) {
                violacoes.adicionar(CODIGO, referencia + "informe o tipo de logradouro.");
            }
            if (emBranco(endereco.logradouro())) {
                violacoes.adicionar(CODIGO, referencia + "informe o logradouro.");
            }
            if (emBranco(endereco.numero())) {
                violacoes.adicionar(CODIGO, referencia + "informe o número.");
            }
            if (emBranco(endereco.bairro())) {
                violacoes.adicionar(CODIGO, referencia + "informe o bairro.");
            }
            if (!cepValido(endereco.cep())) {
                violacoes.adicionar(CODIGO, referencia + "informe um CEP com 8 dígitos.");
            }
            if (emBranco(endereco.cidade())) {
                violacoes.adicionar(CODIGO, referencia + "informe a cidade.");
            }
            if (endereco.uf() == null) {
                violacoes.adicionar(CODIGO, referencia + "informe o estado (UF).");
            }
        }
    }

    private static boolean cepValido(String cep) {
        return cep != null && Telefone.somenteDigitos(cep).length() == 8;
    }

    private static boolean emBranco(String valor) {
        return valor == null || valor.isBlank();
    }
}
