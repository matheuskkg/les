package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import org.hibernate.envers.Audited;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code item_troca} do MER (DEC-12, DEC-16): o item do pedido trocado e a quantidade. Se ele volta ao estoque
 * é informado no recebimento (RF-VEN-014) e fica nulo até lá.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "item_troca", indexes = @Index(name = "ix_item_troca_troca", columnList = "troca_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_item_troca_item", columnNames = {"troca_id", "item_pedido_id"}))
public class ItemTrocaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_troca_seq")
    @SequenceGenerator(name = "item_troca_seq", sequenceName = "item_troca_seq",
            initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "troca_id", nullable = false)
    private TrocaJpa troca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_pedido_id", nullable = false)
    private ItemPedidoJpa itemPedido;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    @Column(name = "volta_estoque")
    private Boolean voltaEstoque;
}
