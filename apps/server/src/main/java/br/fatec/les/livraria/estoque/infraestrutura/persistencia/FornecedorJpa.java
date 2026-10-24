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

/** Fornecedor das entradas de estoque (RN-EST-001, DEC-16). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fornecedor", indexes = @Index(name = "ix_fornecedor_nome", columnList = "nome"))
public class FornecedorJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fornecedor_seq")
    @SequenceGenerator(name = "fornecedor_seq", sequenceName = "fornecedor_seq", initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;
}
