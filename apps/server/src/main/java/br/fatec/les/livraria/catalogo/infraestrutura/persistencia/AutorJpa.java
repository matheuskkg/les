package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Autor de livro (DEC-14, DEC-16). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "autor")
public class AutorJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "autor_seq")
    @SequenceGenerator(name = "autor_seq", sequenceName = "autor_seq", initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;
}
