package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import br.fatec.les.livraria.vendas.dominio.pedido.ClienteNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.EnderecoNoPedido;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code pedido} do MER (DEC-12, DEC-16). Cliente e endereços são snapshots imutáveis em JSON com o id de
 * origem; o {@code cliente_id} também é coluna, só pelo id, para filtrar os pedidos do cliente. O checkout de origem é
 * FK, porque é do mesmo módulo, e o índice com o status atende à busca do pedido em processamento do checkout (DEC-13).
 * O índice de {@code realizado_em} atende ao período e à ordenação padrão da consulta do admin (RF-VEN-009).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "pedido", indexes = {
        @Index(name = "ix_pedido_cliente", columnList = "cliente_id"),
        @Index(name = "ix_pedido_checkout_status", columnList = "checkout_id, status"),
        @Index(name = "ix_pedido_realizado_em", columnList = "realizado_em"),
        @Index(name = "ix_pedido_status_realizado_em", columnList = "status, realizado_em") })
public class PedidoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pedido_seq")
    @SequenceGenerator(name = "pedido_seq", sequenceName = "pedido_seq", initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkout_id")
    private CheckoutJpa checkout;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPedido status;

    @Column(name = "realizado_em", nullable = false)
    private LocalDateTime realizadoEm;

    @Column(name = "entregue_em")
    private LocalDateTime entregueEm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cliente", nullable = false)
    private ClienteNoPedido cliente;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "endereco_entrega", nullable = false)
    private EnderecoNoPedido enderecoEntrega;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "endereco_cobranca", nullable = false)
    private EnderecoNoPedido enderecoCobranca;

    @Column(name = "subtotal_itens", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalItens;

    @Column(name = "frete", nullable = false, precision = 12, scale = 2)
    private BigDecimal frete;

    @Column(name = "desconto_promocional", nullable = false, precision = 12, scale = 2)
    private BigDecimal descontoPromocional;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemPedidoJpa> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PagamentoCartaoJpa> cartoes = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CupomPedidoJpa> cupons = new ArrayList<>();
}
