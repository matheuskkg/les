package br.fatec.les.livraria.clientes.dominio.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Dados obrigatórios do cadastro do cliente (RN-CLI-006): gênero, nome, data de nascimento, CPF, telefone (tipo, DDD
 * e número), e-mail, senha e ao menos um endereço. Em DEC-10 o endereço residencial do DRS vira "ao menos um
 * endereço", de qualquer tipo.
 */
@Component
public class DadosObrigatoriosCliente implements Regra<NovoCliente> {

    private static final String CODIGO = "RN-CLI-006";

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (textoEmBranco(alvo.nome())) {
            violacoes.adicionar(CODIGO, "Informe o nome do cliente.");
        }
        if (alvo.genero() == null) {
            violacoes.adicionar(CODIGO, "Informe o gênero do cliente.");
        }
        if (alvo.dataNascimento() == null) {
            violacoes.adicionar(CODIGO, "Informe a data de nascimento do cliente.");
        }
        if (textoEmBranco(alvo.cpf())) {
            violacoes.adicionar(CODIGO, "Informe o CPF do cliente.");
        }
        if (alvo.tipoTelefone() == null) {
            violacoes.adicionar(CODIGO, "Informe o tipo do telefone.");
        }
        if (!dddValido(alvo.ddd())) {
            violacoes.adicionar(CODIGO, "Informe um DDD com 2 dígitos.");
        }
        if (!numeroDeTelefoneValido(alvo.numero())) {
            violacoes.adicionar(CODIGO, "Informe um número de telefone com 8 ou 9 dígitos.");
        }
        if (textoEmBranco(alvo.email())) {
            violacoes.adicionar(CODIGO, "Informe o e-mail do cliente.");
        }
        if (textoEmBranco(alvo.senha())) {
            violacoes.adicionar(CODIGO, "Informe a senha do cliente.");
        }
        if (alvo.enderecos().isEmpty()) {
            violacoes.adicionar(CODIGO, "Informe ao menos um endereço do cliente.");
        }
    }

    private static boolean textoEmBranco(String valor) {
        return valor == null || valor.isBlank();
    }

    static boolean dddValido(String ddd) {
        return ddd != null && somenteDigitos(ddd).length() == 2;
    }

    static boolean numeroDeTelefoneValido(String numero) {
        if (numero == null) {
            return false;
        }
        int digitos = somenteDigitos(numero).length();
        return digitos == 8 || digitos == 9;
    }

    static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
