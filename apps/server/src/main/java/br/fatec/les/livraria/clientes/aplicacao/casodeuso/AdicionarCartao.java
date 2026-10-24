package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Adiciona um cartão ao cadastro do cliente autenticado (RF-CLI-007, RN-CLI-004, RN-CLI-005, DEC-06, DEC-10). O
 * código de segurança não é recebido nem gravado. O primeiro cartão do cliente vira o preferencial (DEC-10), regra
 * que fica no agregado {@link Cliente}.
 */
@Component
@RequiredArgsConstructor
public class AdicionarCartao {

    private final CartaoCompleto cartaoCompleto; // RN-CLI-004
    private final TamanhosMaximosCartao tamanhosMaximos; // RN-CLI-004
    private final ValidadeDoCartao validadeDoCartao; // DEC-06
    private final BandeiraRegistrada bandeiraRegistrada; // RN-CLI-005
    private final ClienteDoUsuario clienteDoUsuario;
    private final ClienteRepositorio clientes;
    private final BandeiraRepositorio bandeiras;

    /**
     * @return o cartão gravado, já com o id e a marcação de preferencial
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com as violações encontradas
     */
    @Transactional
    public Cartao executar(Long usuarioId, NovoCartao comando) {
        Cliente cliente = clienteDoUsuario.buscarParaAlterar(usuarioId);
        Violacoes violacoes = new Violacoes();
        cartaoCompleto.validar(comando, violacoes);
        tamanhosMaximos.validar(comando, violacoes);
        validadeDoCartao.validar(comando, violacoes);
        Optional<Bandeira> bandeira = bandeiraRegistrada.validar(comando, violacoes, bandeiras);
        if (comando.numero() != null && cliente.temCartaoComNumero(comando.numero())) {
            violacoes.adicionar("RN-CLI-004", "Este cartão já está cadastrado.");
        }
        violacoes.lancarSeHouver();

        Bandeira registrada = bandeira
                .orElseThrow(() -> new IllegalStateException("Bandeira validada não encontrada."));
        Set<Long> existentes = cliente.getCartoes().stream().map(Cartao::id).collect(Collectors.toSet());
        Cliente salvo = clientes.salvar(cliente.comCartao(Cartao.de(comando, registrada, null, false)));
        return salvo.getCartoes().stream()
                .filter(cartao -> !existentes.contains(cartao.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cartão gravado não encontrado no cliente."));
    }
}
