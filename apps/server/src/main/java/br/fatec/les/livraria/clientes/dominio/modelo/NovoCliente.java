package br.fatec.les.livraria.clientes.dominio.modelo;

import java.time.LocalDate;
import java.util.List;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

/**
 * Comando do autocadastro do cliente (RF-CLI-001, RN-CLI-006, DEC-10). Traz os dados crus para que as regras os
 * validem e acumulem todas as violações antes de qualquer gravação.
 */
public record NovoCliente(
        String nome,
        Genero genero,
        LocalDate dataNascimento,
        String cpf,
        String email,
        TipoTelefone tipoTelefone,
        String ddd,
        String numero,
        String senha,
        String confirmacaoSenha,
        List<NovoEndereco> enderecos,
        List<NovoCartao> cartoes) {

    public NovoCliente {
        enderecos = enderecos == null ? List.of() : List.copyOf(enderecos);
        cartoes = cartoes == null ? List.of() : List.copyOf(cartoes);
    }
}
