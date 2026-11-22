package br.fatec.les.livraria.acesso.dominio.modelo;

import br.fatec.les.livraria.compartilhado.dominio.Email;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Identidade de login do sistema (DEC-08, DEC-10): e-mail e telefone servem de login, a senha fica só como hash e a
 * inativação guarda a data que origina a situação da conta (RF-CLI-003).
 */
@Getter
public class Usuario {

    private final Long id;
    private String email;

    /** Opcional: o administrador não tem telefone (DEC-10). */
    private Telefone telefone;

    private String senhaHash;
    private final Papel papel;
    private LocalDateTime inativadoEm;

    public Usuario(Long id, String email, Telefone telefone, String senhaHash, Papel papel, LocalDateTime inativadoEm) {
        this.id = id;
        this.email = new Email(email).endereco();
        this.telefone = telefone;
        this.senhaHash = Objects.requireNonNull(senhaHash, "senhaHash");
        this.papel = Objects.requireNonNull(papel, "papel");
        this.inativadoEm = inativadoEm;
    }

    /** Inativa a conta a partir de {@code agora} (RF-CLI-003); uma conta já inativa não pode ser inativada de novo. */
    public void inativar(LocalDateTime agora) {
        if (inativadoEm != null) {
            throw new IllegalStateException("A conta já está inativa.");
        }
        inativadoEm = Objects.requireNonNull(agora, "agora");
    }

    /** Reativa a conta inativa (RF-CLI-003); uma conta já ativa não pode ser reativada. */
    public void reativar() {
        if (inativadoEm == null) {
            throw new IllegalStateException("A conta já está ativa.");
        }
        inativadoEm = null;
    }

    /**
     * Altera e-mail e telefone de login (RF-CLI-002, DEC-08, DEC-10). O e-mail é normalizado pelo value object, e o
     * telefone já chega validado e só com dígitos.
     */
    public void alterarContato(String email, Telefone telefone) {
        this.email = new Email(email).endereco();
        this.telefone = Objects.requireNonNull(telefone, "telefone");
    }

    /** Troca o hash da senha (RF-CLI-008); o texto puro nunca é guardado (RNF-CLI-003). */
    public void alterarSenhaHash(String senhaHash) {
        this.senhaHash = Objects.requireNonNull(senhaHash, "senhaHash");
    }
}
