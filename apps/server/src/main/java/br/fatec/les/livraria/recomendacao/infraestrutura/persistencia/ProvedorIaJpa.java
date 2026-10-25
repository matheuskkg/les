package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import org.hibernate.envers.Audited;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code provedor_ia} do MER (RNF-REC-001, DEC-03, DEC-14): a ordem e a ativação da cadeia de provedores.
 * O nome é único e igual ao {@code nome()} do adapter de {@code AssistenteDeLivros}. A tabela é auditada porque o
 * ADMIN a altera (RNF-GER-002).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "provedor_ia",
        uniqueConstraints = @UniqueConstraint(name = "uk_provedor_ia_nome", columnNames = "nome"))
public class ProvedorIaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provedor_ia_seq")
    @SequenceGenerator(name = "provedor_ia_seq", sequenceName = "provedor_ia_seq", initialValue = 10000,
            allocationSize = 1)
    private Long id;

    @Column(name = "nome", nullable = false, length = 20)
    private String nome;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;
}
