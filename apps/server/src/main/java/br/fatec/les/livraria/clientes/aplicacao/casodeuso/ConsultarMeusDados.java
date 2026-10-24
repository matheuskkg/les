package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ConsultarContato;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.DadosDeContato;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import lombok.RequiredArgsConstructor;

/**
 * Dados do próprio cliente logado (RF-CLI-002, DEC-07). O cliente é resolvido pelo id do usuário da sessão, nunca por
 * um id na URL, e o e-mail e o telefone vêm do {@code acesso} pela porta pública {@link ConsultarContato}.
 */
@Component
@RequiredArgsConstructor
public class ConsultarMeusDados {

    private final ClienteRepositorio clientes;
    private final ConsultarContato consultarContato;

    @Transactional(readOnly = true)
    public DadosDoCliente executar(Long usuarioId) {
        Cliente cliente = clientes.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
        DadosDeContato contato = consultarContato.executar(usuarioId);
        return DadosDoCliente.de(cliente, contato);
    }
}
