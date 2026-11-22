package br.fatec.les.livraria.clientes.dominio.modelo;

import br.fatec.les.livraria.acesso.dominio.modelo.SenhaInformada;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Comando do autocadastro do cliente (RF-CLI-001, RN-CLI-006, DEC-10). Traz os dados crus para que as regras os validem
 * e acumulem todas as violações antes de qualquer gravação. Os contratos {@link DadosCadastraisCliente} e {@link
 * SenhaInformada} permitem que as mesmas regras puras sejam usadas no cadastro e na alteração de dados.
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
        List<NovoCartao> cartoes)
        implements DadosCadastraisCliente, SenhaInformada {

    public NovoCliente {
        // Elementos nulos são aceitos aqui para que as regras de endereço e cartão os reportem como violação 422,
        // em vez de o List.copyOf lançar NullPointerException e virar erro 500.
        enderecos = enderecos == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(enderecos));
        cartoes = cartoes == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(cartoes));
    }
}
