package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosCliente;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabela {@code cliente} do MER (DEC-14, DEC-16). {@code usuario_id} é apenas a coluna de id: o usuário pertence ao
 * módulo {@code acesso}, então não há {@code @ManyToOne} nem FK (DEC-16).
 */
@Entity
@Audited
@Table(
        name = "cliente",
        indexes = {
            @Index(name = "ix_cliente_cpf", columnList = "cpf"),
            @Index(name = "ix_cliente_usuario_id", columnList = "usuario_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class ClienteJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cliente_seq")
    @SequenceGenerator(name = "cliente_seq", sequenceName = "cliente_seq", initialValue = 10000, allocationSize = 1)
    private Long id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(name = "nome", nullable = false, length = TamanhosMaximosCliente.NOME)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 20)
    private Genero genero;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "cpf", nullable = false, length = TamanhosMaximosCliente.CPF)
    private String cpf;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<EnderecoJpa> enderecos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CartaoJpa> cartoes = new ArrayList<>();
}
