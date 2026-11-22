package br.fatec.les.livraria.clientes.aplicacao.consulta;

import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Dados do cliente que a listagem do admin precisa, sem endereços nem cartões (RF-CLI-004, RNF-CLI-005). O {@code id} é
 * o código exibido do cliente (DEC-14). O CPF fica como texto porque esta é uma leitura da tabela.
 *
 * @param id código do cliente
 * @param usuarioId id do usuário dono do e-mail, do telefone e da situação (DEC-10, DEC-16)
 * @param nome nome completo
 * @param cpf CPF sem máscara
 * @param genero gênero do cliente
 * @param dataNascimento data de nascimento
 */
public record ClienteResumo(Long id, Long usuarioId, String nome, String cpf, Genero genero, LocalDate dataNascimento) {

    public ClienteResumo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(usuarioId, "usuarioId");
        Objects.requireNonNull(nome, "nome");
        Objects.requireNonNull(cpf, "cpf");
        Objects.requireNonNull(genero, "genero");
        Objects.requireNonNull(dataNascimento, "dataNascimento");
    }
}
