package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA do carrinho (RF-VEN-001, DEC-16). A gravação sincroniza os itens pela chave natural (livro): linhas
 * existentes são atualizadas, novas são inseridas e as que saíram do carrinho são removidas ({@code orphanRemoval}),
 * preservando os ids e a auditoria de cada linha.
 */
@Component
@RequiredArgsConstructor
public class CarrinhoRepositorioJpa implements CarrinhoRepositorio {

    private final CarrinhoJpaRepository repositorio;

    @Override
    @Transactional(readOnly = true)
    public Optional<Carrinho> buscarPorCliente(Long clienteId) {
        return repositorio.findByClienteId(clienteId).map(CarrinhoRepositorioJpa::paraDominio);
    }

    @Override
    @Transactional
    public Carrinho salvar(Carrinho carrinho) {
        CarrinhoJpa entidade = carrinho.getId() == null
                ? new CarrinhoJpa()
                : repositorio.findById(carrinho.getId()).orElseThrow(
                        () -> new IllegalStateException("Carrinho não encontrado: " + carrinho.getId()));
        entidade.setClienteId(carrinho.getClienteId());
        entidade.setExpiraEm(carrinho.getExpiraEm());
        entidade.setAvisoEnviadoEm(carrinho.getAvisoEnviadoEm());
        sincronizarItens(entidade, carrinho);
        return paraDominio(repositorio.saveAndFlush(entidade));
    }

    private static void sincronizarItens(CarrinhoJpa entidade, Carrinho carrinho) {
        Map<Long, ItemCarrinho> desejados = carrinho.getItens().stream()
                .collect(Collectors.toMap(ItemCarrinho::getLivroId, Function.identity()));
        entidade.getItens().removeIf(item -> !desejados.containsKey(item.getLivroId()));
        Map<Long, ItemCarrinhoJpa> atuais = entidade.getItens().stream()
                .collect(Collectors.toMap(ItemCarrinhoJpa::getLivroId, Function.identity()));
        for (ItemCarrinho item : carrinho.getItens()) {
            ItemCarrinhoJpa linha = atuais.get(item.getLivroId());
            if (linha == null) {
                linha = new ItemCarrinhoJpa();
                linha.setCarrinho(entidade);
                linha.setLivroId(item.getLivroId());
                entidade.getItens().add(linha);
            }
            linha.setQuantidade(item.getQuantidade());
        }
    }

    private static Carrinho paraDominio(CarrinhoJpa entidade) {
        return new Carrinho(entidade.getId(), entidade.getClienteId(), entidade.getExpiraEm(),
                entidade.getAvisoEnviadoEm(), entidade.getItens().stream()
                        .map(item -> new ItemCarrinho(item.getId(), item.getLivroId(), item.getQuantidade()))
                        .toList());
    }
}
