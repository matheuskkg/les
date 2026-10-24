package br.fatec.les.livraria.acesso.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Colunas do telefone do usuário (DEC-10, DEC-16), nulas quando o usuário não tem telefone. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class TelefoneJpa {

    @Enumerated(EnumType.STRING)
    @Column(name = "telefone_tipo", length = 20)
    private TipoTelefone tipo;

    @Column(name = "telefone_ddd", length = 2)
    private String ddd;

    @Column(name = "telefone_numero", length = 9)
    private String numero;
}
