package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tabela {@code mensagem_conversa} do MER (RNF-REC-001, DEC-03, DEC-14), com os livros citados em {@code
 * mensagem_livro}. {@code chamada_ia_id} aponta para a chamada que gerou a resposta; {@code chamada_ia} não é auditada,
 * então a relação também não.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(
        name = "mensagem_conversa",
        indexes = {
            @Index(name = "ix_mensagem_conversa_conversa", columnList = "conversa_id, enviada_em"),
            @Index(name = "ix_mensagem_conversa_enviada", columnList = "papel, enviada_em")
        })
public class MensagemConversaJpa {

    public static final int TAMANHO_DO_TEXTO = 4000;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mensagem_conversa_seq")
    @SequenceGenerator(
            name = "mensagem_conversa_seq",
            sequenceName = "mensagem_conversa_seq",
            initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversa_id", nullable = false)
    private ConversaJpa conversa;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamada_ia_id", unique = true)
    private ChamadaIaJpa chamadaIa;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 10)
    private PapelDaMensagem papel;

    @Column(name = "texto", nullable = false, length = TAMANHO_DO_TEXTO)
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoDeResposta tipo;

    @Column(name = "enviada_em", nullable = false)
    private LocalDateTime enviadaEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "avaliacao", length = 10)
    private Avaliacao avaliacao;

    @Column(name = "avaliada_em")
    private LocalDateTime avaliadaEm;

    @ElementCollection
    @CollectionTable(
            name = "mensagem_livro",
            joinColumns = @JoinColumn(name = "mensagem_conversa_id"),
            uniqueConstraints =
                    @UniqueConstraint(
                            name = "uk_mensagem_livro",
                            columnNames = {"mensagem_conversa_id", "livro_id"}))
    @OrderBy("posicao")
    private Set<LivroCitadoJpa> livros = new LinkedHashSet<>();
}
