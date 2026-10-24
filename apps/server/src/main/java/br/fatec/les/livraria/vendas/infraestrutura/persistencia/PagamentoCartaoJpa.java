package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import br.fatec.les.livraria.vendas.dominio.pedido.CartaoNoPedido;
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
 * Tabela {@code pagamento_cartao} do MER (DEC-10, DEC-16): o cartão é snapshot em JSON com bandeira e 4 últimos
 * dígitos.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "pagamento_cartao")
public class PagamentoCartaoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pagamento_cartao_seq")
    @SequenceGenerator(name = "pagamento_cartao_seq", sequenceName = "pagamento_cartao_seq",
            initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpa pedido;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cartao", nullable = false)
    private CartaoNoPedido cartao;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
}
