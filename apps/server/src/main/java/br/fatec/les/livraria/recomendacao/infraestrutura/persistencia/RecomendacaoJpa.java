package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabela {@code recomendacao} do MER (RNF-REC-001, DEC-03, DEC-14): cada geração da vitrine de um cliente. {@code
 * cliente_id} é referência a outro módulo, só a coluna; {@code chamada_ia_id} aponta para a chamada que a gerou e é
 * nula na recomendação gravada sem IA. {@code chamada_ia} não é auditada, então a relação também não.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(
        name = "recomendacao",
        indexes = @Index(name = "ix_recomendacao_cliente_gerada", columnList = "cliente_id, gerada_em"))
public class RecomendacaoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recomendacao_seq")
    @SequenceGenerator(
            name = "recomendacao_seq",
            sequenceName = "recomendacao_seq",
            initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamada_ia_id", unique = true)
    private ChamadaIaJpa chamadaIa;

    @Column(name = "gerada_em", nullable = false)
    private LocalDateTime geradaEm;

    @Column(name = "compras_consideradas", nullable = false)
    private int comprasConsideradas;

    @OneToMany(mappedBy = "recomendacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao")
    private List<ItemRecomendacaoJpa> itens = new ArrayList<>();
}
