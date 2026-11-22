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

import org.hibernate.envers.Audited;

/** Editora de livro (DEC-14, DEC-16). */
@Getter
@Setter
@NoArgsConstructor
@Audited
@Entity
@Table(name = "editora")
public class EditoraJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "editora_seq")
    @SequenceGenerator(name = "editora_seq", sequenceName = "editora_seq", initialValue = 10000, allocationSize = 50)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;
}
