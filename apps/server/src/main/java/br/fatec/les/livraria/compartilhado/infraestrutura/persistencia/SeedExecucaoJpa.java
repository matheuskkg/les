package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro do seed já executado (RNF-LIV-002). É uma tabela gerenciada pelo Hibernate para que seja criada e removida
 * junto com o schema (create-drop no H2) e mantida no Postgres (update).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "seed_execucao")
public class SeedExecucaoJpa {

    @Id
    @Column(name = "arquivo", nullable = false, length = 255)
    private String arquivo;

    @Column(name = "checksum", nullable = false, length = 64)
    private String checksum;

    @Column(name = "executado_em", nullable = false)
    private LocalDateTime executadoEm;
}
