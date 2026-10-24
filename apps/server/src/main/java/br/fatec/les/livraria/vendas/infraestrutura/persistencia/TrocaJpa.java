package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.Audited;

import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code troca} do MER (DEC-12, DEC-16): o "pedido de troca" de um pedido entregue. O cupom de troca gerado no
 * recebimento é FK única, nula até lá; o índice do pedido atende à leitura das trocas de cada pedido.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "troca", indexes = {
        @Index(name = "ix_troca_pedido", columnList = "pedido_id"),
        @Index(name = "ix_troca_status", columnList = "status") })
public class TrocaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "troca_seq")
    @SequenceGenerator(name = "troca_seq", sequenceName = "troca_seq", initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpa pedido;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cupom_id", unique = true)
    private CupomJpa cupom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTroca status;

    @Column(name = "solicitada_em", nullable = false)
    private LocalDateTime solicitadaEm;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    @Column(name = "valor_cupom", precision = 12, scale = 2)
    private BigDecimal valorCupom;

    @OneToMany(mappedBy = "troca", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemTrocaJpa> itens = new ArrayList<>();
}
