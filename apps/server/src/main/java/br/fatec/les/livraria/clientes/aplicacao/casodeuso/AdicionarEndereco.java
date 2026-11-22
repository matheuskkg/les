package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.clientes.dominio.regra.EnderecoCompleto;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosEndereco;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adiciona um endereço ao cadastro do cliente autenticado (RF-CLI-006, RNF-CLI-004). Não altera os demais dados do
 * cadastro; o endereço é incluído no agregado {@link Cliente}, que mantém ao menos um endereço (RN-CLI-001, RN-CLI-002,
 * DEC-10). As regras de composição e tamanho são as do próprio endereço (RN-CLI-003).
 */
@Component
@RequiredArgsConstructor
public class AdicionarEndereco {

    private final EnderecoCompleto enderecoCompleto; // RN-CLI-003
    private final TamanhosMaximosEndereco tamanhosMaximos; // RN-CLI-003
    private final ClienteDoUsuario clienteDoUsuario;
    private final ClienteRepositorio clientes;

    /**
     * @return o endereço gravado, já com o id gerado
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com as violações encontradas
     */
    @Transactional
    public Endereco executar(Long usuarioId, NovoEndereco comando) {
        Violacoes.verificar(comando, enderecoCompleto, tamanhosMaximos);
        Cliente cliente = clienteDoUsuario.buscarParaAlterar(usuarioId);
        Set<Long> existentes = cliente.getEnderecos().stream().map(Endereco::id).collect(Collectors.toSet());
        Cliente salvo = clientes.salvar(cliente.comEndereco(Endereco.de(comando, null)));
        return salvo.getEnderecos().stream()
                .filter(endereco -> !existentes.contains(endereco.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Endereço gravado não encontrado no cliente."));
    }
}
