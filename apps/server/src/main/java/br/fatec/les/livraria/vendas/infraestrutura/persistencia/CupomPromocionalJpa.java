package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.envers.Audited;

import br.fatec.les.livraria.vendas.dominio.pagamento.TipoCupomPromocional;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code cupom_promocional} do MER (DEC-11, DEC-15.2): criada pelo seed, sem situação; o uso por cliente fica
 * em {@code uso_cupom_promocional}. O código é guardado em maiúsculas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "cupom_promocional")
public class CupomPromocionalJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cupom_promocional_seq")
    @SequenceGenerator(name = "cupom_promocional_seq", sequenceName = "cupom_promocional_seq",
            initialValue = 10000, allocationSize = 50)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoCupomPromocional tipo;

    @Column(name = "valor", precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "validade", nullable = false)
    private LocalDate validade;

    @Column(name = "valor_minimo", precision = 12, scale = 2)
    private BigDecimal valorMinimo;
}
