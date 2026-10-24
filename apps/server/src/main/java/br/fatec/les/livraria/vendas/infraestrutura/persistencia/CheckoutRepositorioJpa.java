package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.vendas.aplicacao.porta.CheckoutRepositorio;
import br.fatec.les.livraria.vendas.dominio.modelo.Checkout;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCheckout;
import br.fatec.les.livraria.vendas.dominio.modelo.SituacaoCheckout;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA do checkout (RF-VEN-003, DEC-12, DEC-16). Os itens são gravados na criação e não mudam depois: para
 * alterar quantidades, o cliente volta ao carrinho. A versão lida é conferida na gravação (DEC-13).
 */
@Component
@RequiredArgsConstructor
public class CheckoutRepositorioJpa implements CheckoutRepositorio {

    private final CheckoutJpaRepository repositorio;

    @Override
    @Transactional(readOnly = true)
    public Optional<Checkout> buscarAbertoPorCliente(Long clienteId) {
        return repositorio.findFirstByClienteIdAndSituacao(clienteId, SituacaoCheckout.ABERTO)
                .map(CheckoutRepositorioJpa::paraDominio);
    }

    @Override
    @Transactional
    public Optional<Checkout> buscarAbertoPorClienteParaAlterar(Long clienteId) {
        return repositorio.buscarPorClienteComLock(clienteId, SituacaoCheckout.ABERTO)
                .map(CheckoutRepositorioJpa::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> buscarClienteId(Long checkoutId) {
        return repositorio.buscarClienteId(checkoutId);
    }

    @Override
    @Transactional
    public Optional<Checkout> buscarParaAlterar(Long checkoutId) {
        return repositorio.buscarPorIdComLock(checkoutId).map(CheckoutRepositorioJpa::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarIdsParaAvisar(LocalDateTime agora, LocalDateTime limite) {
        return repositorio.buscarIdsParaAvisar(SituacaoCheckout.ABERTO, agora, limite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarIdsVencidos(LocalDateTime agora) {
        return repositorio.buscarIdsVencidos(SituacaoCheckout.ABERTO, agora);
    }

    @Override
    @Transactional
    public Checkout salvar(Checkout checkout) {
        CheckoutJpa entidade;
        if (checkout.getId() == null) {
            entidade = new CheckoutJpa();
            entidade.setClienteId(checkout.getClienteId());
            entidade.setIniciadoEm(checkout.getIniciadoEm());
            entidade.setExpiraEm(checkout.getExpiraEm());
            for (ItemCheckout item : checkout.getItens()) {
                ItemCheckoutJpa linha = new ItemCheckoutJpa();
                linha.setCheckout(entidade);
                linha.setLivroId(item.livroId());
                linha.setQuantidade(item.quantidade());
                entidade.getItens().add(linha);
            }
        } else {
            entidade = repositorio.findById(checkout.getId()).orElseThrow(
                    () -> new IllegalStateException("Checkout não encontrado: " + checkout.getId()));
            if (!Objects.equals(entidade.getVersion(), checkout.getVersao())) {
                throw new ObjectOptimisticLockingFailureException(CheckoutJpa.class, checkout.getId());
            }
        }
        entidade.setSituacao(checkout.getSituacao());
        entidade.setAvisoEnviadoEm(checkout.getAvisoEnviadoEm());
        return paraDominio(repositorio.saveAndFlush(entidade));
    }

    private static Checkout paraDominio(CheckoutJpa entidade) {
        return new Checkout(entidade.getId(), entidade.getClienteId(), entidade.getSituacao(),
                entidade.getIniciadoEm(), entidade.getExpiraEm(), entidade.getAvisoEnviadoEm(), entidade.getVersion(),
                entidade.getItens().stream()
                        .map(item -> new ItemCheckout(item.getLivroId(), item.getQuantidade()))
                        .toList());
    }
}
