package br.fatec.les.livraria.clientes.dominio.modelo;

import java.time.LocalDate;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;

/**
 * Dados cadastrais do cliente que o cadastro e a alteração compartilham (RF-CLI-002, RN-CLI-006, DEC-10). As regras
 * puras de nome, gênero, nascimento, CPF, e-mail e telefone validam este contrato, de modo que a alteração reusa as
 * mesmas validações do cadastro.
 */
public interface DadosCadastraisCliente {

    String nome();

    Genero genero();

    LocalDate dataNascimento();

    String cpf();

    String email();

    TipoTelefone tipoTelefone();

    String ddd();

    String numero();
}
