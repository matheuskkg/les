package br.fatec.les.livraria.clientes.dominio.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
        // Elementos nulos são aceitos aqui para que as regras de endereço e cartão os reportem como violação 422,
        // em vez de o List.copyOf lançar NullPointerException e virar erro 500.
        enderecos = enderecos == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(enderecos));
        cartoes = cartoes == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(cartoes));
    }
}
