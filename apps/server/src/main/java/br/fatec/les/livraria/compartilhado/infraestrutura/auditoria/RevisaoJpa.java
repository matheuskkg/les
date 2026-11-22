package br.fatec.les.livraria.compartilhado.infraestrutura.auditoria;

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

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.LocalDateTime;

/**
 * Revisão do Hibernate Envers com o autor e a data/hora da escrita (RNF-GER-002, DEC-08). As tabelas {@code _aud}
 * apontam para cá pela coluna {@code revisao} (DEC-14).
 *
 * <p>O id usa a sequence {@code revisao_seq} a partir de 10000, como as demais entidades (DEC-14).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@RevisionEntity(RevisaoListener.class)
@Table(name = "revisao")
public class RevisaoJpa {

    @Id
    @RevisionNumber
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revisao_seq")
    @SequenceGenerator(name = "revisao_seq", sequenceName = "revisao_seq", initialValue = 10000, allocationSize = 50)
    private Long id;

    @RevisionTimestamp
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    /** Usuário autenticado responsável pela escrita; nulo sem login e fora de requisição HTTP (DEC-08). */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** E-mail do usuário autenticado, {@code anonimo} ou {@code sistema} (DEC-08). */
    @Column(name = "usuario", nullable = false, length = 254)
    private String usuario;
}
