package br.fatec.les.livraria.clientes.dominio.modelo;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import br.fatec.les.livraria.compartilhado.dominio.Cpf;
import lombok.Getter;

/**
 * Cliente do e-commerce (RF-CLI-001, RN-CLI-006, DEC-10). O e-mail e o telefone ficam no {@code Usuario} do módulo
 * {@code acesso}; aqui há só o id dele (DEC-16). O código exibido é o próprio id (RNF-CLI-005).
 */
@Getter
public class Cliente {

    private final Long id;
    private final Long usuarioId;
    private final String nome;
    private final Genero genero;
    private final LocalDate dataNascimento;
    private final Cpf cpf;
    private final List<Endereco> enderecos;
    private final List<Cartao> cartoes;

    public Cliente(Long id, Long usuarioId, String nome, Genero genero, LocalDate dataNascimento, Cpf cpf,
            List<Endereco> enderecos, List<Cartao> cartoes) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId");
        this.nome = Objects.requireNonNull(nome, "nome");
        this.genero = Objects.requireNonNull(genero, "genero");
        this.dataNascimento = Objects.requireNonNull(dataNascimento, "dataNascimento");
        this.cpf = Objects.requireNonNull(cpf, "cpf");
        this.enderecos = List.copyOf(enderecos);
        this.cartoes = List.copyOf(cartoes);
        if (this.enderecos.isEmpty()) {
            throw new IllegalArgumentException("O cliente exige ao menos um endereço (RN-CLI-001, RN-CLI-002).");
        }
    }

    /** Código único do cliente (RNF-CLI-005, DEC-10). */
    public Long codigo() {
        return id;
    }
}
