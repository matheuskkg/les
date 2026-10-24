package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

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

/** Categoria de livro (DEC-14, DEC-16). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categoria", indexes = @Index(name = "ix_categoria_nome", columnList = "nome"))
public class CategoriaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "categoria_seq")
    @SequenceGenerator(name = "categoria_seq", sequenceName = "categoria_seq", initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @Column(name = "nome", nullable = false, length = 60)
    private String nome;
}
