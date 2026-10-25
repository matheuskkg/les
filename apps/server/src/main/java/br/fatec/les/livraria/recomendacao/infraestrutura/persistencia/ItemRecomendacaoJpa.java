package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import java.time.LocalDateTime;

import org.hibernate.envers.Audited;

import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;
import jakarta.persistence.Column;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code item_recomendacao} do MER (RNF-REC-001, DEC-03, DEC-14): livro, posição, justificativa e a última
 * avaliação do cliente. {@code livro_id} é referência a outro módulo, só a coluna.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "item_recomendacao",
        indexes = @Index(name = "ix_item_recomendacao_recomendacao", columnList = "recomendacao_id"))
public class ItemRecomendacaoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_recomendacao_seq")
    @SequenceGenerator(name = "item_recomendacao_seq", sequenceName = "item_recomendacao_seq",
            initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recomendacao_id", nullable = false)
    private RecomendacaoJpa recomendacao;

    @Column(name = "livro_id", nullable = false)
    private Long livroId;

    @Column(name = "posicao", nullable = false)
    private int posicao;

    @Column(name = "justificativa", length = 300)
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(name = "avaliacao", length = 10)
    private Avaliacao avaliacao;

    @Column(name = "avaliada_em")
    private LocalDateTime avaliadaEm;
}
