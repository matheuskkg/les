package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.regra.BandeiraRegistrada;
import br.fatec.les.livraria.clientes.aplicacao.regra.ValidadeDoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.regra.CartaoCompleto;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosCartao;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Altera os dados de um cartão do cliente autenticado (RF-CLI-007, RN-CLI-004, RN-CLI-005, DEC-06, DEC-10): número,
 * nome impresso, bandeira e validade. O preferencial não muda por aqui — para isso existe
 * {@link MarcarCartaoPreferencial} — e o código de segurança não é recebido nem gravado (DEC-10). Cartão de outro
 * cliente responde 404 (DEC-07).
 */
@Component
@RequiredArgsConstructor
public class AlterarCartao {

    private final CartaoCompleto cartaoCompleto; // RN-CLI-004
    private final TamanhosMaximosCartao tamanhosMaximos; // RN-CLI-004
    private final ValidadeDoCartao validadeDoCartao; // DEC-06
    private final BandeiraRegistrada bandeiraRegistrada; // RN-CLI-005
    private final ClienteDoUsuario clienteDoUsuario;
    private final ClienteRepositorio clientes;
    private final BandeiraRepositorio bandeiras;

    /**
     * @return o cartão gravado, preservando a marcação de preferencial
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com as violações encontradas
     * @throws ExcecaoDeRecursoNaoEncontrado se o cartão não for do cliente autenticado
     */
    @Transactional
    public Cartao executar(Long usuarioId, AlteracaoDeCartao comando) {
        Cliente cliente = clienteDoUsuario.buscarParaAlterar(usuarioId);
        Cartao atual = cliente.cartao(comando.id())
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cartão não encontrado."));

        // O número só volta mascarado na API: em branco, a edição mantém o número gravado (DEC-10).
        NovoCartao informado = comando.dados();
        NovoCartao dados = informado.numero() == null || informado.numero().isBlank()
                ? new NovoCartao(atual.numero(), informado.nomeImpresso(), informado.bandeiraId(), informado.validade())
                : informado;
        Violacoes violacoes = new Violacoes();
        cartaoCompleto.validar(dados, violacoes);
        tamanhosMaximos.validar(dados, violacoes);
        validadeDoCartao.validar(dados, violacoes);
        Optional<Bandeira> bandeira = bandeiraRegistrada.validar(dados, violacoes, bandeiras);
        violacoes.lancarSeHouver();

        Bandeira registrada = bandeira
                .orElseThrow(() -> new IllegalStateException("Bandeira validada não encontrada."));
        Cartao alterado = Cartao.de(dados, registrada, atual.id(), atual.preferencial());
        Cliente salvo = clientes.salvar(cliente.comCartaoAtualizado(alterado));
        return salvo.cartao(atual.id())
                .orElseThrow(() -> new IllegalStateException("Cartão alterado não encontrado no cliente."));
    }
}
