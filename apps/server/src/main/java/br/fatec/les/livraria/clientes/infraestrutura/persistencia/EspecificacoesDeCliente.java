package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import br.fatec.les.livraria.clientes.aplicacao.consulta.FiltroCliente;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.Especificacoes;

/**
 * Traduz o {@link FiltroCliente} para as {@link Specification} da consulta de clientes do admin (RF-CLI-004, DEC-07).
 * Os campos se combinam com E; gênero, UF e situação repetidos combinam com OU. O filtro por cidade e UF usa
 * {@code exists} no endereço, para não duplicar linhas nem quebrar a contagem da paginação.
 */
final class EspecificacoesDeCliente {

    private EspecificacoesDeCliente() {
    }

    static Specification<ClienteJpa> de(FiltroCliente filtro) {
        Objects.requireNonNull(filtro, "filtro");
        return Specification.<ClienteJpa>unrestricted()
                .and(Especificacoes.contem("nome", filtro.nome()))
                .and(Especificacoes.digitos("cpf", filtro.cpf()))
                .and(Especificacoes.em("genero", filtro.genero()))
                .and(Especificacoes.entre("dataNascimento",
                        filtro.dataNascimentoDe(), filtro.dataNascimentoAte()))
                .and(Especificacoes.existeContem("enderecos", "cidade", filtro.cidade()))
                .and(Especificacoes.existe("enderecos", "uf", filtro.uf()))
                .and(porUsuario("usuarioId", filtro.usuarioIdsDoContato()))
                .and(porUsuario("usuarioId", filtro.usuarioIdsDaSituacao()));
    }

    /** Um filtro resolvido no {@code acesso}: ausente não filtra; vazio não encontra ninguém (DEC-10). */
    private static Specification<ClienteJpa> porUsuario(String campo, Optional<Set<Long>> ids) {
        if (ids.isEmpty()) {
            return Specification.unrestricted();
        }
        Set<Long> valores = ids.get();
        return valores.isEmpty() ? Especificacoes.nenhum() : Especificacoes.em(campo, valores);
    }
}
