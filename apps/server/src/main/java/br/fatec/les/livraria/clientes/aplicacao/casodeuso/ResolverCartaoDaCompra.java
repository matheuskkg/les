package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.regra.ValidadeDoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeCartao;
import br.fatec.les.livraria.clientes.dominio.regra.BandeiraRegistrada;
import br.fatec.les.livraria.clientes.dominio.regra.CartaoCompleto;
import br.fatec.les.livraria.clientes.dominio.regra.EscolhaDeCartaoValida;
import br.fatec.les.livraria.clientes.dominio.regra.NumeroDoCartaoValido;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosCartao;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAtual;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Resolve o cartão escolhido numa compra pelo cliente autenticado (RF-VEN-006, DEC-10): um dos cartões dele ou um
 * novo, validado como no cadastro (RN-CLI-004, RN-CLI-005), inclusive a validade (DEC-06). Um cartão salvo não tem a
 * validade conferida aqui: vencido depois de cadastrado, é a operadora que o reprova (DEC-05.2). Só lê: o cartão novo
 * não é gravado, e o código de segurança nunca passa por aqui.
 */
@Component
@RequiredArgsConstructor
public class ResolverCartaoDaCompra {

    private final EscolhaDeCartaoValida escolhaValida; // RF-VEN-006
    private final CartaoCompleto cartaoCompleto; // RN-CLI-004
    private final TamanhosMaximosCartao tamanhosMaximos; // RN-CLI-004
    private final ValidadeDoCartao validadeDoCartao; // DEC-06
    private final BandeiraRegistrada bandeiraRegistrada; // RN-CLI-005
    private final NumeroDoCartaoValido numeroDoCartaoValido; // RN-CLI-004
    private final BandeiraRepositorio bandeiras;
    private final ClienteDoUsuario clienteDoUsuario;
    private final UsuarioAtual usuarioAtual;

    /**
     * @return o cartão cadastrado, com o id, ou o novo, sem id
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se a escolha ou o cartão novo forem
     *         inválidos
     * @throws ExcecaoDeRecursoNaoEncontrado se o cartão escolhido não for do cliente autenticado
     */
    @Transactional(readOnly = true)
    public Cartao executar(EscolhaDeCartao escolha) {
        Violacoes.verificar(escolha, escolhaValida);
        if (escolha.novo() != null) {
            List<Bandeira> registradas = bandeiras.listar();
            Violacoes violacoes = new Violacoes();
            cartaoCompleto.validar(escolha.novo(), violacoes);
            tamanhosMaximos.validar(escolha.novo(), violacoes);
            validadeDoCartao.validar(escolha.novo(), violacoes);
            Optional<Bandeira> bandeira = bandeiraRegistrada.identificar(escolha.novo(), violacoes, registradas);
            numeroDoCartaoValido.validar(escolha.novo(), violacoes, registradas);
            violacoes.lancarSeHouver();
            return Cartao.de(escolha.novo(), bandeira
                    .orElseThrow(() -> new IllegalStateException("Bandeira validada não encontrada.")), null, false);
        }

        Long usuarioId = usuarioAtual.usuarioId()
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
        return clienteDoUsuario.buscar(usuarioId).getCartoes().stream()
                .filter(cartao -> cartao.id().equals(escolha.cartaoId()))
                .findFirst()
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cartão não encontrado."));
    }
}
