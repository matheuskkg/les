package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.OrdenacaoPermitida;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Monta a {@link Paginacao} a partir dos query params {@code pagina}, {@code tamanho} e {@code ordem} de uma listagem,
 * validando a ordenação contra os campos declarados (DEC-07, RNF-GER-001, RF-CLI-004, RF-LIV-005).
 */
@Component
public class CriadorDePaginacao {

    /**
     * @param pagina página a partir de 0; {@code null} usa o padrão
     * @param tamanho itens por página; {@code null} usa o padrão
     * @param ordem valores repetíveis no formato {@code campo,asc|desc}; vazio usa a ordem padrão da listagem
     * @param permitida campos ordenáveis aceitos pela listagem
     */
    public Paginacao criar(Integer pagina, Integer tamanho, List<String> ordem, OrdenacaoPermitida permitida) {
        int paginaEfetiva = pagina == null ? Paginacao.PAGINA_PADRAO : pagina;
        int tamanhoEfetivo = tamanho == null ? Paginacao.TAMANHO_PADRAO : tamanho;
        List<Ordem> informadas = ordens(ordem);
        List<Ordem> escolhidas = informadas.isEmpty() ? permitida.padrao() : informadas;
        List<Ordem> traduzidas =
                escolhidas.stream().map(o -> new Ordem(permitida.propriedade(o.campo()), o.direcao())).toList();
        return new Paginacao(paginaEfetiva, tamanhoEfetivo, traduzidas);
    }

    private static List<Ordem> ordens(List<String> valores) {
        if (valores == null) {
            return List.of();
        }
        List<Ordem> resultado = new ArrayList<>();
        for (String valor : valores) {
            if (valor == null || valor.isBlank()) {
                continue;
            }
            resultado.add(Ordem.de(valor));
        }
        return List.copyOf(resultado);
    }
}
