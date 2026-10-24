package br.fatec.les.livraria.estoque.aplicacao.casodeuso;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.aplicacao.Ids;
import br.fatec.les.livraria.estoque.aplicacao.porta.MaiorCustoPorLivro;
import lombok.RequiredArgsConstructor;

/**
 * Expõe ao restante do sistema o maior custo por livro entre as entradas de estoque (RN-EST-002,
 * DEC-15.3). É a porta de entrada pública do módulo {@code estoque} para o preço de venda
 * (RF-EST-002); os outros módulos não acessam a persistência de estoque.
 */
@Component
@RequiredArgsConstructor
public class ConsultarMaiorCusto {

    private final MaiorCustoPorLivro maiorCustoPorLivro;

    /**
     * @param livroIds livros a consultar; não pode ser nulo nem conter elementos nulos
     * @return maior custo por id de livro; livro sem entrada não aparece no mapa
     */
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> executar(Collection<Long> livroIds) {
        if (Ids.validar(livroIds)) {
            return Map.of();
        }
        return maiorCustoPorLivro.buscar(livroIds);
    }
}
