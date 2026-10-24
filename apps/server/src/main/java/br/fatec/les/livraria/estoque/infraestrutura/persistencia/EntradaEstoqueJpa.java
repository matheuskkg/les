package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.LocalDate;

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

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entrada de estoque: histórico com quantidade, custo, fornecedor e data (RN-EST-001). O maior custo
 * do livro entre as entradas define o preço de venda (RN-EST-002).
 *
 * <p>{@code livro_id} é referência a outro módulo: só a coluna, sem {@code @ManyToOne} e sem FK
 * (DEC-16).
 */
@Getter
@Setter
@NoArgsConstructor
@Audited
@Entity
@Table(name = "entrada_estoque", indexes = {
        @Index(name = "ix_entrada_estoque_livro_data", columnList = "livro_id, data_entrada"),
        @Index(name = "ix_entrada_estoque_fornecedor", columnList = "fornecedor_id") })
public class EntradaEstoqueJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entrada_estoque_seq")
    @SequenceGenerator(name = "entrada_estoque_seq", sequenceName = "entrada_estoque_seq",
            initialValue = 10000, allocationSize = 50)
    private Long id;

    @Column(name = "livro_id", nullable = false)
    private Long livroId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private FornecedorJpa fornecedor;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "custo", nullable = false, precision = 12, scale = 2)
    private BigDecimal custo;

    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;
}
