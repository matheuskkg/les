package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import br.fatec.les.livraria.clientes.dominio.modelo.TipoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoLogradouro;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosCliente;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.envers.Audited;

/** Tabela {@code endereco} do MER (RN-CLI-003, DEC-10, DEC-14, DEC-16), com FK para o cliente do mesmo módulo. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "endereco", indexes = @Index(name = "ix_endereco_cliente_id", columnList = "cliente_id"))
public class EnderecoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "endereco_seq")
    @SequenceGenerator(name = "endereco_seq", sequenceName = "endereco_seq", initialValue = 10000,
            allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteJpa cliente;

    @Column(name = "nome_curto", nullable = false, length = TamanhosMaximosCliente.NOME_CURTO)
    private String nomeCurto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_endereco", nullable = false, length = 20)
    private TipoEndereco tipoEndereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_logradouro", nullable = false, length = 20)
    private TipoLogradouro tipoLogradouro;

    @Column(name = "logradouro", nullable = false, length = TamanhosMaximosCliente.LOGRADOURO)
    private String logradouro;

    @Column(name = "numero", nullable = false, length = TamanhosMaximosCliente.NUMERO_DO_ENDERECO)
    private String numero;

    @Column(name = "bairro", nullable = false, length = TamanhosMaximosCliente.BAIRRO)
    private String bairro;

    @Column(name = "cep", nullable = false, length = TamanhosMaximosCliente.CEP)
    private String cep;

    @Column(name = "cidade", nullable = false, length = TamanhosMaximosCliente.CIDADE)
    private String cidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "uf", nullable = false, length = 2)
    private Uf uf;

    @Column(name = "pais", nullable = false, length = 40)
    private String pais;

    @Column(name = "observacoes", length = TamanhosMaximosCliente.OBSERVACOES)
    private String observacoes;
}
