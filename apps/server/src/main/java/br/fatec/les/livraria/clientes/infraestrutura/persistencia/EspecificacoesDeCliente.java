package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import br.fatec.les.livraria.clientes.aplicacao.consulta.FiltroCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.Especificacoes;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Traduz o {@link FiltroCliente} para as {@link Specification} da consulta de clientes do admin (RF-CLI-004, DEC-07).
 * Os campos se combinam com E; gênero, UF e situação repetidos combinam com OU. O filtro por cidade e UF usa {@code
 * exists} no endereço, para não duplicar linhas nem quebrar a contagem da paginação.
 */
final class EspecificacoesDeCliente {

    private EspecificacoesDeCliente() {}

    static Specification<ClienteJpa> de(FiltroCliente filtro) {
        Objects.requireNonNull(filtro, "filtro");
        return porNomeECpf(filtro.nome(), filtro.cpf())
                .and(Especificacoes.em("genero", filtro.genero()))
                .and(Especificacoes.entre("dataNascimento", filtro.dataNascimentoDe(), filtro.dataNascimentoAte()))
                .and(enderecoCom(filtro.cidade(), filtro.uf()))
                .and(porUsuario("usuarioId", filtro.usuarioIdsDoContato()))
                .and(porUsuario("usuarioId", filtro.usuarioIdsDaSituacao()));
    }

    /** Nome ("contém", sem caixa nem acentos) e CPF (só os dígitos), combinados com E (DEC-07). */
    static Specification<ClienteJpa> porNomeECpf(String nome, String cpf) {
        return Specification.<ClienteJpa>unrestricted()
                .and(Especificacoes.contem("nome", nome))
                .and(Especificacoes.digitos("cpf", cpf));
    }

    /**
     * Cidade e UF valem para o mesmo endereço: um só {@code exists}, para que "Campinas" e "RJ" não casem com endereços
     * diferentes do cliente (RF-CLI-004, DEC-07).
     */
    private static Specification<ClienteJpa> enderecoCom(String cidade, List<Uf> ufs) {
        boolean semCidade = cidade == null || cidade.isBlank();
        if (semCidade && ufs.isEmpty()) {
            return Specification.unrestricted();
        }
        return (raiz, consulta, cb) -> {
            Subquery<Integer> subconsulta = consulta.subquery(Integer.class);
            Root<ClienteJpa> externa = subconsulta.correlate(raiz);
            Join<ClienteJpa, EnderecoJpa> endereco = externa.join("enderecos");
            List<Predicate> condicoes = new ArrayList<>();
            Predicate naCidade = Especificacoes.contemEm(cb, endereco.get("cidade"), cidade);
            if (naCidade != null) {
                condicoes.add(naCidade);
            }
            if (!ufs.isEmpty()) {
                condicoes.add(endereco.get("uf").in(ufs));
            }
            subconsulta.select(cb.literal(1)).where(condicoes.toArray(Predicate[]::new));
            return cb.exists(subconsulta);
        };
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
