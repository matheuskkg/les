package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroParaVenda;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Ids;
import lombok.RequiredArgsConstructor;

/**
 * Expõe ao {@code vendas} o título e os autores dos livros de um carrinho, inclusive os inativos (RF-VEN-001,
 * DEC-07). É a porta de entrada pública do catálogo para a leitura do carrinho; o preço continua vindo de
 * {@link ConsultarPrecosDeVenda}.
 */
@Component
@RequiredArgsConstructor
public class ConsultarLivrosParaVenda {

    private final CatalogoConsultas catalogoConsultas;

    /**
     * @param livroIds livros a consultar; não pode ser nulo nem conter elementos nulos
     * @return livro por id, incluindo inativos; id inexistente não aparece no mapa
     */
    @Transactional(readOnly = true)
    public Map<Long, LivroParaVenda> executar(Collection<Long> livroIds) {
        if (Ids.validar(livroIds)) {
            return Map.of();
        }

        Map<Long, LivroParaVenda> livros = new HashMap<>();
        for (LivroParaVenda livro : catalogoConsultas.buscarParaVenda(livroIds)) {
            livros.put(livro.id(), livro);
        }
        return Map.copyOf(livros);
    }
}
