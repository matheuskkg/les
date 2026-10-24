package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

/** Entidade exclusiva dos testes das listagens; não faz parte do contrato da API. */
@Getter
@Entity
@Table(name = "exemplo_listagem")
public class ExemploListagemJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exemplo_listagem_seq")
    @SequenceGenerator(name = "exemplo_listagem_seq", sequenceName = "exemplo_listagem_seq", initialValue = 10000)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String codigo;

    private String cpf;

    private String uf;

    private BigDecimal valor;

    @Column(name = "data_cadastro")
    private LocalDate dataCadastro;

    @ManyToMany
    @JoinTable(name = "exemplo_listagem_tag",
            joinColumns = @JoinColumn(name = "exemplo_listagem_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<ExemploTagJpa> tags = new HashSet<>();

    protected ExemploListagemJpa() {
    }

    public ExemploListagemJpa(String nome, String codigo, String cpf, String uf, BigDecimal valor,
            LocalDate dataCadastro) {
        this.nome = nome;
        this.codigo = codigo;
        this.cpf = cpf;
        this.uf = uf;
        this.valor = valor;
        this.dataCadastro = dataCadastro;
    }

    public void adicionarTag(ExemploTagJpa tag) {
        tags.add(tag);
    }
}
