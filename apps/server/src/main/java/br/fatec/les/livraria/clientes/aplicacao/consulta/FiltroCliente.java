package br.fatec.les.livraria.clientes.aplicacao.consulta;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;

/**
 * Filtro da porta de consulta de clientes do admin (RF-CLI-004, DEC-07). Os campos da tabela {@code cliente} e dos
 * endereços se combinam com E; gênero e UF repetidos se combinam com OU. E-mail, telefone e situação vêm do módulo
 * {@code acesso} já resolvidos em ids de usuário (DEC-10): um {@link Optional} vazio não filtra, e um conjunto
 * vazio significa que nenhum usuário atendeu ao filtro.
 *
 * @param nome trecho do nome, sem diferenciar maiúsculas nem acentos
 * @param cpf CPF comparado só pelos dígitos
 * @param genero gêneros desejados; vazio não filtra
 * @param dataNascimentoDe data de nascimento inicial, inclusive
 * @param dataNascimentoAte data de nascimento final, inclusive
 * @param cidade trecho da cidade de algum endereço, sem diferenciar maiúsculas nem acentos
 * @param uf UFs de algum endereço; vazio não filtra
 * @param usuarioIdsDoContato ids dos usuários que atendem a e-mail e/ou telefone
 * @param usuarioIdsDaSituacao ids dos usuários nas situações pedidas
 */
public record FiltroCliente(
        String nome,
        String cpf,
        List<Genero> genero,
        LocalDate dataNascimentoDe,
        LocalDate dataNascimentoAte,
        String cidade,
        List<Uf> uf,
        Optional<Set<Long>> usuarioIdsDoContato,
        Optional<Set<Long>> usuarioIdsDaSituacao) {

    public FiltroCliente {
        genero = genero == null ? List.of() : genero.stream().filter(Objects::nonNull).toList();
        uf = uf == null ? List.of() : uf.stream().filter(Objects::nonNull).toList();
        usuarioIdsDoContato = copiar(usuarioIdsDoContato);
        usuarioIdsDaSituacao = copiar(usuarioIdsDaSituacao);
    }

    private static Optional<Set<Long>> copiar(Optional<Set<Long>> ids) {
        return ids == null ? Optional.empty() : ids.map(Set::copyOf);
    }
}
