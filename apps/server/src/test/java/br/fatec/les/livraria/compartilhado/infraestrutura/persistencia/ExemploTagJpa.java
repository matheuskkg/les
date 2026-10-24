package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

/** Entidade auxiliar dos testes de filtro por relacionamento muitos-para-muitos. */
@Getter
@Entity
@Table(name = "exemplo_tag")
public class ExemploTagJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exemplo_tag_seq")
    @SequenceGenerator(name = "exemplo_tag_seq", sequenceName = "exemplo_tag_seq", initialValue = 10000)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    protected ExemploTagJpa() {
    }

    public ExemploTagJpa(String nome) {
        this.nome = nome;
    }
}
