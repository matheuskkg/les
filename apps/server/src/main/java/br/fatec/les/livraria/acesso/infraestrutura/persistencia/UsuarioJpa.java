package br.fatec.les.livraria.acesso.infraestrutura.persistencia;

import br.fatec.les.livraria.acesso.dominio.modelo.Papel;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;

/** Tabela {@code usuario} do MER (DEC-14, DEC-16). A situação não é gravada: é derivada de {@code inativado_em}. */
@Audited
@Entity
@Table(
        name = "usuario",
        indexes = {
            @Index(name = "ix_usuario_email", columnList = "email"),
            @Index(name = "ix_usuario_telefone", columnList = "telefone_ddd, telefone_numero")
        })
@Getter
@Setter
@NoArgsConstructor
public class UsuarioJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(name = "usuario_seq", sequenceName = "usuario_seq", initialValue = 10000, allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    /** Fora da auditoria: {@code usuario_aud} nunca guarda o hash da senha (RNF-CLI-003). */
    @NotAudited
    @Column(name = "senha_hash", nullable = false, length = 72)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel;

    @Column(name = "inativado_em")
    private LocalDateTime inativadoEm;

    /** Opcional: o administrador não tem telefone (DEC-10). */
    @Embedded private TelefoneJpa telefone;
}
