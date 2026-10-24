package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import org.springframework.http.ResponseCookie;

/** Token de sessão recém-emitido e o cookie que o transporta (DEC-08). */
public record TokenDeSessao(String valor, ResponseCookie cookie) {
}
