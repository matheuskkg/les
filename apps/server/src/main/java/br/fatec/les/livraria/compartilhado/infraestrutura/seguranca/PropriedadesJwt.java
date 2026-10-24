package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuração do token de sessão (DEC-08).
 *
 * @param segredo chave HMAC, com no mínimo 32 bytes em UTF-8
 * @param validade prazo de validade do token e do cookie
 */
@Validated
@ConfigurationProperties("livraria.jwt")
public record PropriedadesJwt(
        @NotBlank String segredo,
        @NotNull @DefaultValue("8h") Duration validade) {

    public static final int TAMANHO_MINIMO_DO_SEGREDO = 32;

    public PropriedadesJwt {
        if (segredo != null && segredo.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_DO_SEGREDO) {
            throw new IllegalArgumentException("livraria.jwt.segredo deve ter ao menos "
                    + TAMANHO_MINIMO_DO_SEGREDO + " bytes em UTF-8.");
        }
        if (validade != null && (validade.isZero() || validade.isNegative())) {
            throw new IllegalArgumentException("livraria.jwt.validade deve ser positiva.");
        }
    }
}
