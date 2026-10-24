package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import br.fatec.les.livraria.vendas.dominio.pedido.LivroNoPedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code item_pedido} do MER (DEC-12, DEC-16): o livro é snapshot em JSON com o id de origem e as categorias,
 * sem {@code livro_id}; preço unitário e quantidade são colunas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "item_pedido")
public class ItemPedidoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_pedido_seq")
    @SequenceGenerator(name = "item_pedido_seq", sequenceName = "item_pedido_seq",
            initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpa pedido;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "livro", nullable = false)
    private LivroNoPedido livro;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;
}
