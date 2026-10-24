package br.fatec.les.livraria.acesso.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.fatec.les.livraria.acesso.dominio.modelo.Papel;

/** Consultas da tabela {@code usuario}; o filtro de "conta em uso" recebe o limite de tempo do adapter. */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpa, Long> {

    /*
     * Devolve lista ordenada do id mais recente para o mais antigo: a unicidade de e-mail e telefone é garantida na
     * aplicação, então duas contas em uso podem combinar, e o adapter escolhe sempre a mais recente sem lançar
     * IncorrectResultSizeDataAccessException.
     */
    @Query("""
            select u from UsuarioJpa u
            where u.email = :email
              and (u.inativadoEm is null or u.inativadoEm >= :limite)
            order by u.id desc
            """)
    List<UsuarioJpa> buscarPorEmailEmUso(@Param("email") String email, @Param("limite") LocalDateTime limite);

    @Query("""
            select u from UsuarioJpa u
            where u.telefone.ddd = :ddd
              and u.telefone.numero = :numero
              and (u.inativadoEm is null or u.inativadoEm >= :limite)
            order by u.id desc
            """)
    List<UsuarioJpa> buscarPorTelefoneEmUso(@Param("ddd") String ddd, @Param("numero") String numero,
            @Param("limite") LocalDateTime limite);

    @Query("""
            select count(u) from UsuarioJpa u
            where u.email = :email
              and (u.inativadoEm is null or u.inativadoEm >= :limite)
            """)
    long contarPorEmailEmUso(@Param("email") String email, @Param("limite") LocalDateTime limite);

    @Query("""
            select count(u) from UsuarioJpa u
            where u.telefone.ddd = :ddd
              and u.telefone.numero = :numero
              and (u.inativadoEm is null or u.inativadoEm >= :limite)
            """)
    long contarPorTelefoneEmUso(@Param("ddd") String ddd, @Param("numero") String numero,
            @Param("limite") LocalDateTime limite);

    @Query("select u.id from UsuarioJpa u where u.papel = :papel order by u.id")
    List<Long> listarIdsPorPapel(@Param("papel") Papel papel);
}
