package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import br.fatec.les.livraria.vendas.aplicacao.porta.CarrinhoRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemRemovidoCarrinho;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adapter JPA do carrinho (RF-VEN-001, DEC-16). A gravação sincroniza os itens pela chave natural (livro): linhas
 * existentes são atualizadas, novas são inseridas e as que saíram do carrinho são removidas ({@code orphanRemoval}),
 * preservando os ids e a auditoria de cada linha. Os itens removidos por expiração são sincronizados do mesmo jeito.
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
    public Optional<Carrinho> buscarPorClienteParaAlterar(Long clienteId) {
        return repositorio.buscarPorClienteComLock(clienteId).map(CarrinhoRepositorioJpa::paraDominio);
    }

    @Override
    @Transactional
    public Optional<Carrinho> buscarParaAlterar(Long carrinhoId) {
        return repositorio.buscarPorIdComLock(carrinhoId).map(CarrinhoRepositorioJpa::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarIdsParaAvisar(LocalDateTime agora, LocalDateTime limite) {
        return repositorio.buscarIdsParaAvisar(agora, limite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarIdsVencidos(LocalDateTime agora) {
        return repositorio.buscarIdsVencidos(agora);
    }

    @Override
    @Transactional
    public Carrinho salvar(Carrinho carrinho) {
        CarrinhoJpa entidade =
                carrinho.getId() == null
                        ? new CarrinhoJpa()
                        : repositorio
                                .findById(carrinho.getId())
                                .orElseThrow(() ->
                                        new IllegalStateException("Carrinho não encontrado: " + carrinho.getId()));
        entidade.setClienteId(carrinho.getClienteId());
        entidade.setExpiraEm(carrinho.getExpiraEm());
        entidade.setAvisoEnviadoEm(carrinho.getAvisoEnviadoEm());
        sincronizarItens(entidade, carrinho);
        sincronizarRemovidos(entidade, carrinho);
        return paraDominio(repositorio.saveAndFlush(entidade));
    }

    private static void sincronizarItens(CarrinhoJpa entidade, Carrinho carrinho) {
        Map<Long, ItemCarrinho> desejados =
                carrinho.getItens().stream().collect(Collectors.toMap(ItemCarrinho::getLivroId, Function.identity()));
        entidade.getItens().removeIf(item -> !desejados.containsKey(item.getLivroId()));
        Map<Long, ItemCarrinhoJpa> atuais =
                entidade.getItens().stream()
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

    private static void sincronizarRemovidos(CarrinhoJpa entidade, Carrinho carrinho) {
        Map<Long, ItemRemovidoCarrinho> desejados =
                carrinho.getRemovidos().stream()
                        .collect(Collectors.toMap(ItemRemovidoCarrinho::livroId, Function.identity()));
        entidade.getRemovidos().removeIf(removido -> !desejados.containsKey(removido.getLivroId()));
        Map<Long, ItemRemovidoCarrinhoJpa> atuais =
                entidade.getRemovidos().stream()
                        .collect(Collectors.toMap(ItemRemovidoCarrinhoJpa::getLivroId, Function.identity()));
        for (ItemRemovidoCarrinho removido : carrinho.getRemovidos()) {
            ItemRemovidoCarrinhoJpa linha = atuais.get(removido.livroId());
            if (linha == null) {
                linha = new ItemRemovidoCarrinhoJpa();
                linha.setCarrinho(entidade);
                linha.setLivroId(removido.livroId());
                entidade.getRemovidos().add(linha);
            }
            linha.setQuantidade(removido.quantidade());
            linha.setRemovidoEm(removido.removidoEm());
        }
    }

    private static Carrinho paraDominio(CarrinhoJpa entidade) {
        return new Carrinho(
                entidade.getId(),
                entidade.getClienteId(),
                entidade.getExpiraEm(),
                entidade.getAvisoEnviadoEm(),
                entidade.getItens().stream()
                        .map(item -> new ItemCarrinho(item.getId(), item.getLivroId(), item.getQuantidade()))
                        .toList(),
                entidade.getRemovidos().stream()
                        .map(removido ->
                                new ItemRemovidoCarrinho(
                                        removido.getLivroId(), removido.getQuantidade(), removido.getRemovidoEm()))
                        .toList());
    }
}
