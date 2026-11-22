package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/**
 * Dados obrigatórios do cadastro do cliente (RN-CLI-006): gênero, nome, data de nascimento, CPF, telefone (tipo, DDD e
 * número), e-mail e senha. Em DEC-10 o endereço residencial do DRS vira "ao menos um endereço", de qualquer tipo,
 * verificado por {@link EnderecoDoCliente}.
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
        if (!Telefone.dddValido(alvo.ddd())) {
            violacoes.adicionar(CODIGO, "Informe um DDD com 2 dígitos.");
        }
        if (!Telefone.numeroValido(alvo.numero())) {
            violacoes.adicionar(CODIGO, "Informe um número de telefone com 8 ou 9 dígitos.");
        }
        if (textoEmBranco(alvo.email())) {
            violacoes.adicionar(CODIGO, "Informe o e-mail do cliente.");
        }
        if (textoEmBranco(alvo.senha())) {
            violacoes.adicionar(CODIGO, "Informe a senha do cliente.");
        }
    }

    private static boolean textoEmBranco(String valor) {
        return valor == null || valor.isBlank();
    }
}
