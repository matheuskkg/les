package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Consultas da tabela {@code cliente}; a unicidade de CPF consulta o {@code acesso} depois (DEC-10). */
public interface ClienteJpaRepository extends JpaRepository<ClienteJpa, Long> {

    @Query("select c.usuarioId from ClienteJpa c where c.cpf = :cpf")
    List<Long> buscarUsuarioIdsPorCpf(@Param("cpf") String cpf);
}
