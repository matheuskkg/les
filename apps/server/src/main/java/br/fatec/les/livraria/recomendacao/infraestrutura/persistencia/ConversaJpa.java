package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

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

import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

/**
 * Tabela {@code conversa} do MER (RNF-REC-001, DEC-03, DEC-14): a conversa contínua do cliente com o assistente. A
 * ativa é a sem {@code arquivada_em}. {@code cliente_id} é referência a outro módulo, só a coluna.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "conversa", indexes = @Index(name = "ix_conversa_cliente", columnList = "cliente_id, arquivada_em"))
public class ConversaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conversa_seq")
    @SequenceGenerator(name = "conversa_seq", sequenceName = "conversa_seq", initialValue = 10000, allocationSize = 50)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "iniciada_em", nullable = false)
    private LocalDateTime iniciadaEm;

    @Column(name = "arquivada_em")
    private LocalDateTime arquivadaEm;
}
