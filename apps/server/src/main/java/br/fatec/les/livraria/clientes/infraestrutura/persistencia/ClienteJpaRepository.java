package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/** Consultas da tabela {@code cliente}; a unicidade de CPF consulta o {@code acesso} depois (DEC-10). */
public interface ClienteJpaRepository extends JpaRepository<ClienteJpa, Long>, JpaSpecificationExecutor<ClienteJpa> {

    Optional<ClienteJpa> findByUsuarioId(Long usuarioId);

    /** Trava a linha do cliente: as escritas de endereços e cartões ficam em fila (cartão preferencial único). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ClienteJpa c where c.usuarioId = :usuarioId")
    Optional<ClienteJpa> buscarPorUsuarioIdParaAlterar(@Param("usuarioId") Long usuarioId);

    @Query("select c.usuarioId from ClienteJpa c where c.cpf = :cpf")
    List<Long> buscarUsuarioIdsPorCpf(@Param("cpf") String cpf);

    /** Id do cliente do usuário de login, sem carregar endereços e cartões (DEC-07, DEC-15.4). */
    @Query("select c.id from ClienteJpa c where c.usuarioId = :usuarioId")
    Optional<Long> buscarIdPorUsuarioId(@Param("usuarioId") Long usuarioId);
}
