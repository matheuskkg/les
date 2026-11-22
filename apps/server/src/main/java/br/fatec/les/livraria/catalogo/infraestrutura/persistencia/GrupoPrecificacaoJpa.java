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

import java.math.BigDecimal;

/** Grupo de precificação com a margem de lucro usada em RF-EST-002/RN-EST-002 (DEC-14, DEC-16). */
@Getter
@Setter
@NoArgsConstructor
@Audited
@Entity
@Table(name = "grupo_precificacao")
public class GrupoPrecificacaoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "grupo_precificacao_seq")
    @SequenceGenerator(
            name = "grupo_precificacao_seq",
            sequenceName = "grupo_precificacao_seq",
            initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @Column(name = "nome", nullable = false, length = 60)
    private String nome;

    @Column(name = "margem", nullable = false, precision = 5, scale = 4)
    private BigDecimal margem;
}
