package br.fatec.les.livraria.recomendacao.infraestrutura.web.dto;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import br.fatec.les.livraria.recomendacao.dominio.modelo.UltimaFalha;

/** Última tentativa sem sucesso de um provedor, sem a mensagem de erro (RNF-REC-001, DEC-03). */
public record UltimaFalhaDto(LocalDateTime em, ResultadoDaChamada resultado, @Nullable Integer codigoHttp) {

    public static @Nullable UltimaFalhaDto de(@Nullable UltimaFalha falha) {
        return falha == null ? null : new UltimaFalhaDto(falha.em(), falha.resultado(), falha.codigoHttp());
    }
}
