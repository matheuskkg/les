package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeEndereco;
import br.fatec.les.livraria.clientes.dominio.regra.EnderecoCompleto;
import br.fatec.les.livraria.clientes.dominio.regra.EscolhaDeEnderecoValida;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosEndereco;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAtual;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Resolve o endereço escolhido numa compra pelo cliente autenticado (RF-VEN-005, DEC-10): um dos endereços dele ou um
 * novo, validado como no cadastro (RN-CLI-003). Só lê: o endereço novo não é gravado aqui, e o {@code vendas} recebe
 * os dados para calcular o frete (RF-VEN-004) e para a cópia no pedido (DEC-12).
 */
@Component
@RequiredArgsConstructor
public class ResolverEnderecoDaCompra {

    private final EscolhaDeEnderecoValida escolhaValida; // RF-VEN-005
    private final EnderecoCompleto enderecoCompleto; // RN-CLI-003
    private final TamanhosMaximosEndereco tamanhosMaximos; // RN-CLI-003
    private final ClienteDoUsuario clienteDoUsuario;
    private final UsuarioAtual usuarioAtual;

    /**
     * @return o endereço cadastrado, com o id, ou o novo, sem id
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se a escolha ou o endereço novo forem
     *         inválidos
     * @throws ExcecaoDeRecursoNaoEncontrado se o endereço escolhido não for do cliente autenticado
     */
    @Transactional(readOnly = true)
    public Endereco executar(EscolhaDeEndereco escolha) {
        Violacoes.verificar(escolha, escolhaValida);
        if (escolha.novo() != null) {
            Violacoes.verificar(escolha.novo(), enderecoCompleto, tamanhosMaximos);
            return Endereco.de(escolha.novo(), null);
        }

        Long usuarioId = usuarioAtual.usuarioId()
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
        return clienteDoUsuario.buscar(usuarioId).getEnderecos().stream()
                .filter(endereco -> endereco.id().equals(escolha.enderecoId()))
                .findFirst()
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Endereço não encontrado."));
    }
}
