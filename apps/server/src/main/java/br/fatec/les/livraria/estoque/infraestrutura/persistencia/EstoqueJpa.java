package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Check;
import org.hibernate.envers.Audited;

/**
 * Saldo em mãos por livro (DEC-15.3). O {@code livro_id} é único e referência a outro módulo, sem
 * {@code @ManyToOne} e sem FK (DEC-16).
 */
@Getter
@Setter
@NoArgsConstructor
@Audited
@Entity
@Check(constraints = "quantidade >= 0")
@Table(name = "estoque", indexes = @Index(name = "uk_estoque_livro", columnList = "livro_id", unique = true))
public class EstoqueJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estoque_seq")
    @SequenceGenerator(name = "estoque_seq", sequenceName = "estoque_seq", initialValue = 10000, allocationSize = 50)
    private Long id;

    @Column(name = "livro_id", nullable = false)
    private Long livroId;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;
}
