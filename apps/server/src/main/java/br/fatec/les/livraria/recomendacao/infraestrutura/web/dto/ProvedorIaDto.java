package br.fatec.les.livraria.recomendacao.infraestrutura.web.dto;

import br.fatec.les.livraria.recomendacao.aplicacao.consulta.ProvedorConfigurado;

import org.jspecify.annotations.Nullable;

/**
 * Provedor da cadeia de IA na tela do ADMIN (RNF-REC-001, DEC-03). Informa só se há chave configurada: a chave e a URL
 * nunca são expostas.
 */
public record ProvedorIaDto(
        Long id,
        String nome,
        int ordem,
        boolean ativo,
        @Nullable String modelo,
        boolean chaveConfigurada,
        @Nullable UltimaFalhaDto ultimaFalha) {

    public static ProvedorIaDto de(ProvedorConfigurado configurado) {
        return new ProvedorIaDto(
                configurado.provedor().id(),
                configurado.provedor().nome(),
                configurado.provedor().ordem(),
                configurado.provedor().ativo(),
                configurado.modelo(),
                configurado.chaveConfigurada(),
                UltimaFalhaDto.de(configurado.ultimaFalha()));
    }
}
