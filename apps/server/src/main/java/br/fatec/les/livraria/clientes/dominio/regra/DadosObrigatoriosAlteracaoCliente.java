package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.modelo.DadosCadastraisCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/**
 * Dados obrigatórios da alteração dos dados do cliente (RF-CLI-002, RN-CLI-006, DEC-10): nome, gênero, data de
 * nascimento, e-mail e telefone (tipo, DDD e número). O CPF é opcional na alteração: quando informado, é validado por
 * {@link CpfValido}; quando omitido, o valor atual é mantido. Senha e endereços têm operações próprias.
 */
@Component
public class DadosObrigatoriosAlteracaoCliente implements Regra<DadosCadastraisCliente> {

    private static final String CODIGO = "RN-CLI-006";

    @Override
    public void validar(DadosCadastraisCliente alvo, Violacoes violacoes) {
        if (textoEmBranco(alvo.nome())) {
            violacoes.adicionar(CODIGO, "Informe o nome do cliente.");
        }
        if (alvo.genero() == null) {
            violacoes.adicionar(CODIGO, "Informe o gênero do cliente.");
        }
        if (alvo.dataNascimento() == null) {
            violacoes.adicionar(CODIGO, "Informe a data de nascimento do cliente.");
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
    }

    private static boolean textoEmBranco(String valor) {
        return valor == null || valor.isBlank();
    }
}
