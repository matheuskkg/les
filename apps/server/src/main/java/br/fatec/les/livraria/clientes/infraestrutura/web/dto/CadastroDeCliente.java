package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import java.time.LocalDate;
import java.util.List;

import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

/**
 * Corpo de {@code POST /api/clientes} (RF-CLI-001, DEC-07, DEC-10). Os campos sem validação de formato deixam as
 * regras de negócio acumularem as violações e responderem 422.
 */
public record CadastroDeCliente(
        String nome,
        Genero genero,
        LocalDate dataNascimento,
        String cpf,
        String email,
        TelefoneDoCadastro telefone,
        String senha,
        String confirmacaoSenha,
        List<EnderecoDoCadastro> enderecos,
        List<CartaoDoCadastro> cartoes) {
}
