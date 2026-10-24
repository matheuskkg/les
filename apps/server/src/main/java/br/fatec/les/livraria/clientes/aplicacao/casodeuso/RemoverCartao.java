package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.time.YearMonth;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import lombok.RequiredArgsConstructor;

/**
 * Exclui fisicamente um cartão do cliente autenticado (RF-CLI-007, DEC-10). Se era o preferencial, o cartão restante
 * mais recente ainda válido (DEC-06) assume a preferência; a regra fica no agregado {@link Cliente}. Cartão de outro cliente responde 404
 * (DEC-07); os pedidos antigos não mudam, porque guardam só a bandeira e os 4 últimos dígitos (DEC-16).
 */
@Component
@RequiredArgsConstructor
public class RemoverCartao {

    private final ClienteDoUsuario clienteDoUsuario;
    private final ClienteRepositorio clientes;
    private final Relogio relogio;

    /**
     * @throws ExcecaoDeRecursoNaoEncontrado se o cartão não for do cliente autenticado
     */
    @Transactional
    public void executar(Long usuarioId, Long cartaoId) {
        Cliente cliente = clienteDoUsuario.buscarParaAlterar(usuarioId);
        if (cliente.cartao(cartaoId).isEmpty()) {
            throw new ExcecaoDeRecursoNaoEncontrado("Cartão não encontrado.");
        }
        clientes.salvar(cliente.semCartao(cartaoId, YearMonth.from(relogio.agora())));
    }
}
