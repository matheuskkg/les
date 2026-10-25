package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.envers.Audited;

/** Tabela {@code bandeira} do MER (RN-CLI-005, DEC-14, DEC-16); o seed é do card CLI-01. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "bandeira")
public class BandeiraJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bandeira_seq")
    @SequenceGenerator(name = "bandeira_seq", sequenceName = "bandeira_seq", initialValue = 10000,
            allocationSize = 1)
    private Long id;

    @Column(name = "nome", nullable = false, length = 40)
    private String nome;

    /** Prefixos ou faixas inclusivas separados por vírgula (RN-CLI-005, DEC-18). */
    @Column(name = "prefixos", nullable = false, length = 500)
    private String prefixos;

    /** Comprimentos aceitos separados por vírgula (RN-CLI-004, DEC-18). */
    @Column(name = "tamanhos", nullable = false, length = 20)
    private String tamanhos;
}
