package br.fatec.les.livraria.catalogo.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Livro do catálogo (RN-LIV-001, RN-LIV-002). As dimensões e o peso alimentam o cálculo de frete
 * (DEC-05.3), e o grupo de precificação guarda a margem usada em RF-EST-002/RN-EST-002.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "livro", indexes = {
        @Index(name = "ix_livro_titulo", columnList = "titulo"),
        @Index(name = "ix_livro_isbn", columnList = "isbn"),
        @Index(name = "ix_livro_ativo", columnList = "ativo"),
        @Index(name = "ix_livro_editora", columnList = "editora_id"),
        @Index(name = "ix_livro_grupo_precificacao", columnList = "grupo_precificacao_id") })
public class LivroJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "livro_seq")
    @SequenceGenerator(name = "livro_seq", sequenceName = "livro_seq", initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "editora_id", nullable = false)
    private EditoraJpa editora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_precificacao_id", nullable = false)
    private GrupoPrecificacaoJpa grupoPrecificacao;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "edicao", nullable = false, length = 40)
    private String edicao;

    @Column(name = "isbn", nullable = false, length = 13)
    private String isbn;

    @Column(name = "paginas", nullable = false)
    private Integer paginas;

    @Column(name = "sinopse", nullable = false, columnDefinition = "text")
    private String sinopse;

    @Column(name = "altura", nullable = false, precision = 6, scale = 2)
    private BigDecimal altura;

    @Column(name = "largura", nullable = false, precision = 6, scale = 2)
    private BigDecimal largura;

    @Column(name = "profundidade", nullable = false, precision = 6, scale = 2)
    private BigDecimal profundidade;

    @Column(name = "peso", nullable = false, precision = 6, scale = 3)
    private BigDecimal peso;

    @Column(name = "codigo_barras", nullable = false, length = 20)
    private String codigoBarras;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "livro_autor",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id"),
            indexes = @Index(name = "ix_livro_autor_autor_id", columnList = "autor_id"))
    private Set<AutorJpa> autores = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "livro_categoria",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"),
            indexes = @Index(name = "ix_livro_categoria_categoria_id", columnList = "categoria_id"))
    private Set<CategoriaJpa> categorias = new LinkedHashSet<>();
}
