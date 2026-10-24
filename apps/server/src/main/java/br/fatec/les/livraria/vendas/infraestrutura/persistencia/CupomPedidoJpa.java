package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import br.fatec.les.livraria.vendas.dominio.pedido.CupomNoPedido;
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
 * Tabela {@code cupom_pedido} do MER (DEC-12, DEC-16): o cupom é snapshot em JSON com o id de origem e a categoria, sem
 * {@code cupom_id}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "cupom_pedido")
public class CupomPedidoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cupom_pedido_seq")
    @SequenceGenerator(name = "cupom_pedido_seq", sequenceName = "cupom_pedido_seq",
            initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpa pedido;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cupom", nullable = false)
    private CupomNoPedido cupom;

    @Column(name = "valor_abatido", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAbatido;
}
