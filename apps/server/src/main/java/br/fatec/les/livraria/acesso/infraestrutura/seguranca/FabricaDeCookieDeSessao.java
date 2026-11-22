package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import br.fatec.les.livraria.compartilhado.infraestrutura.seguranca.PropriedadesJwt;
import br.fatec.les.livraria.compartilhado.infraestrutura.seguranca.PropriedadesSessao;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Monta o cookie {@code sessao} com os atributos do DEC-08, tanto na emissão quanto na limpeza. */
@Component
@RequiredArgsConstructor
public class FabricaDeCookieDeSessao {

    private final PropriedadesJwt propriedadesJwt;
    private final PropriedadesSessao propriedadesSessao;

    public ResponseCookie criar(String valor) {
        return cookie(valor).maxAge(propriedadesJwt.validade()).build();
    }

    /** Cookie que apaga a sessão no navegador (DEC-08). */
    public ResponseCookie expirado() {
        return cookie("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder cookie(String valor) {
        return ResponseCookie.from(EmissorDeToken.COOKIE, valor)
                .httpOnly(true)
                .secure(propriedadesSessao.cookieSeguro())
                .sameSite("Strict")
                .path("/api");
    }
}
