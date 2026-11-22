package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import br.fatec.les.livraria.vendas.dominio.pagamento.SituacaoCupom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tabela {@code cupom} do MER (DEC-11, DEC-15.2): só cupons de troca, de um cliente, com {@code @Version} contra dois
 * pedidos simultâneos usando o mesmo cupom. O {@code cliente_id} é referência ao módulo {@code clientes} só pelo id.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "cupom", indexes = @Index(name = "ix_cupom_cliente_situacao", columnList = "cliente_id, situacao"))
public class CupomJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cupom_seq")
    @SequenceGenerator(name = "cupom_seq", sequenceName = "cupom_seq", initialValue = 10000, allocationSize = 50)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "validade")
    private LocalDate validade;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoCupom situacao;

    @Column(name = "gerado_em", nullable = false)
    private LocalDateTime geradoEm;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
