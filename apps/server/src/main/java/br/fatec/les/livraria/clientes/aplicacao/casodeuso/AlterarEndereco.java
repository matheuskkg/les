package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.clientes.dominio.regra.EnderecoCompleto;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosEndereco;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Altera um endereço do cliente autenticado, sem mexer nos demais dados cadastrais (RF-CLI-006, RNF-CLI-004). O id
 * vem da sessão, então endereço de outro cliente responde 404 (DEC-07). As regras são as da composição e do tamanho
 * do endereço (RN-CLI-003).
 */
@Component
@RequiredArgsConstructor
public class AlterarEndereco {

    private final EnderecoCompleto enderecoCompleto; // RN-CLI-003
    private final TamanhosMaximosEndereco tamanhosMaximos; // RN-CLI-003
    private final ClienteDoUsuario clienteDoUsuario;
    private final ClienteRepositorio clientes;

    /**
     * @return o endereço gravado
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com as violações encontradas
     * @throws ExcecaoDeRecursoNaoEncontrado se o endereço não for do cliente autenticado
     */
    @Transactional
    public Endereco executar(Long usuarioId, AlteracaoDeEndereco comando) {
        Cliente cliente = clienteDoUsuario.buscarParaAlterar(usuarioId);
        Endereco atual = cliente.endereco(comando.id())
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Endereço não encontrado."));
        NovoEndereco dados = comando.dados();
        Violacoes.verificar(dados, enderecoCompleto, tamanhosMaximos);
        Cliente salvo = clientes.salvar(cliente.comEnderecoAtualizado(Endereco.de(dados, atual.id())));
        return salvo.endereco(atual.id())
                .orElseThrow(() -> new IllegalStateException("Endereço alterado não encontrado no cliente."));
    }
}
