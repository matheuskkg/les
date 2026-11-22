package br.fatec.les.livraria.acesso.aplicacao.consulta;

import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * E-mail, telefone e situação de um usuário, carregados em lote para a consulta de clientes do admin (RF-CLI-004,
 * DEC-07, DEC-10). O telefone é nulo para o administrador, que não tem esse dado.
 *
 * @param usuarioId id do usuário dono dos dados de contato
 * @param email e-mail de login
 * @param telefone telefone com DDD e número, ou nulo
 * @param situacao situação calculada da conta no instante da consulta
 */
public record ContatoDoUsuario(Long usuarioId, String email, @Nullable Telefone telefone, SituacaoConta situacao) {

    public ContatoDoUsuario {
        Objects.requireNonNull(usuarioId, "usuarioId");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(situacao, "situacao");
    }
}
