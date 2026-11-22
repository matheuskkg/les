package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.clientes.dominio.modelo.DadosCadastraisCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

import java.time.LocalDate;

/**
 * Comando da alteração dos dados do cliente (RF-CLI-002, RN-CLI-006, DEC-10). O {@code usuarioId} vem da sessão, nunca
 * do corpo ou da URL, e é usado para ignorar a própria conta nas unicidades de CPF, e-mail e telefone. O CPF é
 * opcional: quando nulo ou em branco, o valor atual é mantido.
 */
public record DadosAlteradosDoCliente(
        Long usuarioId,
        String nome,
        Genero genero,
        LocalDate dataNascimento,
        String cpf,
        String email,
        TipoTelefone tipoTelefone,
        String ddd,
        String numero)
        implements DadosCadastraisCliente {}
