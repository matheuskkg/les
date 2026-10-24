package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.CriarUsuario;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.NovoUsuario;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.regra.BandeiraRegistrada;
import br.fatec.les.livraria.clientes.aplicacao.regra.CpfUnico;
import br.fatec.les.livraria.clientes.aplicacao.regra.DataNascimentoValida;
import br.fatec.les.livraria.clientes.aplicacao.regra.EmailUnico;
import br.fatec.les.livraria.clientes.aplicacao.regra.TelefoneUnico;
import br.fatec.les.livraria.clientes.aplicacao.regra.ValidadeDoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.clientes.dominio.regra.CartaoCompleto;
import br.fatec.les.livraria.clientes.dominio.regra.ConfirmacaoDeSenha;
import br.fatec.les.livraria.clientes.dominio.regra.CpfValido;
import br.fatec.les.livraria.clientes.dominio.regra.DadosObrigatoriosCliente;
import br.fatec.les.livraria.clientes.dominio.regra.EmailValido;
import br.fatec.les.livraria.clientes.dominio.regra.EnderecoCompleto;
import br.fatec.les.livraria.clientes.dominio.regra.EnderecoDoCliente;
import br.fatec.les.livraria.clientes.dominio.regra.SenhaForte;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosCliente;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;
import br.fatec.les.livraria.compartilhado.dominio.Email;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Autocadastro do cliente (RF-CLI-001, RN-CLI-001 a RN-CLI-006, RNF-CLI-001, RNF-CLI-002, RNF-CLI-005). Executa todas
 * as regras antes de gravar — acumulando as violações — e, em uma única transação (DEC-08), cria o {@code Usuario} no
 * {@code acesso} e o {@code Cliente} com endereços e cartões. O id devolvido é o código do cliente (DEC-10).
 */
@Component
@RequiredArgsConstructor
public class CadastrarCliente {

    private final DadosObrigatoriosCliente dadosObrigatorios; // RN-CLI-006
    private final DataNascimentoValida dataNascimentoValida; // RN-CLI-006
    private final EnderecoDoCliente enderecoDoCliente; // RN-CLI-001, RN-CLI-002
    private final EnderecoCompleto enderecoCompleto; // RN-CLI-003
    private final TamanhosMaximosCliente tamanhosMaximos; // RN-CLI-003, RN-CLI-004, RN-CLI-006
    private final SenhaForte senhaForte; // RNF-CLI-001
    private final ConfirmacaoDeSenha confirmacaoDeSenha; // RNF-CLI-002
    private final CpfValido cpfValido;
    private final EmailValido emailValido;
    private final CartaoCompleto cartaoCompleto; // RN-CLI-004
    private final ValidadeDoCartao validadeDoCartao; // DEC-06
    private final CpfUnico cpfUnico; // RF-CLI-001
    private final EmailUnico emailUnico; // RF-CLI-001
    private final TelefoneUnico telefoneUnico; // RF-CLI-001
    private final BandeiraRegistrada bandeiraRegistrada; // RN-CLI-005
    private final CriarUsuario criarUsuario;
    private final ClienteRepositorio clientes;
    private final BandeiraRepositorio bandeiras;

    /**
     * @return o id (código, RNF-CLI-005) do cliente criado
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com todas as violações encontradas
     */
    @Transactional
    public Long executar(NovoCliente comando) {
        // As bandeiras são consultadas uma única vez por id e reaproveitadas na validação e na montagem dos cartões.
        Map<Long, Bandeira> bandeirasPorId = bandeirasDosCartoes(comando);
        Regra<NovoCliente> bandeiraRegistradaComAsBandeiras = (alvo, violacoes) ->
                bandeiraRegistrada.validar(alvo, violacoes, bandeirasPorId);

        Violacoes.verificar(comando, dadosObrigatorios, dataNascimentoValida, enderecoDoCliente,
                enderecoCompleto, tamanhosMaximos, senhaForte, confirmacaoDeSenha, cpfValido, emailValido,
                cartaoCompleto, validadeDoCartao, cpfUnico, emailUnico, telefoneUnico,
                bandeiraRegistradaComAsBandeiras);

        Long usuarioId = criarUsuario.executar(new NovoUsuario(
                new Email(comando.email()).endereco(),
                comando.tipoTelefone(),
                Telefone.somenteDigitos(comando.ddd()),
                Telefone.somenteDigitos(comando.numero()),
                comando.senha()));

        Cliente cliente = new Cliente(null, usuarioId, comando.nome().strip(), comando.genero(),
                comando.dataNascimento(), new Cpf(comando.cpf()),
                enderecos(comando), cartoes(comando, bandeirasPorId));
        return clientes.salvar(cliente).getId();
    }

    private Map<Long, Bandeira> bandeirasDosCartoes(NovoCliente comando) {
        Set<Long> ids = new HashSet<>();
        for (NovoCartao cartao : comando.cartoes()) {
            if (cartao != null && cartao.bandeiraId() != null) {
                ids.add(cartao.bandeiraId());
            }
        }
        Map<Long, Bandeira> bandeirasPorId = new HashMap<>();
        for (Long id : ids) {
            bandeiras.buscarPorId(id).ifPresent(bandeira -> bandeirasPorId.put(id, bandeira));
        }
        return bandeirasPorId;
    }

    private List<Endereco> enderecos(NovoCliente comando) {
        List<Endereco> enderecos = new ArrayList<>();
        for (NovoEndereco novo : comando.enderecos()) {
            enderecos.add(new Endereco(null, novo.nomeCurto().strip(), novo.tipoEndereco(),
                    novo.tipoLogradouro(), novo.logradouro().strip(), novo.numero().strip(),
                    novo.bairro().strip(), Telefone.somenteDigitos(novo.cep()), novo.cidade().strip(), novo.uf(),
                    Endereco.PAIS, textoOuNulo(novo.observacoes())));
        }
        return enderecos;
    }

    /**
     * Monta os cartões: o primeiro vira o preferencial (RF-CLI-007, DEC-10). A bandeira já foi validada e carregada
     * por {@code BandeiraRegistrada}.
     */
    private List<Cartao> cartoes(NovoCliente comando, Map<Long, Bandeira> bandeirasPorId) {
        List<Cartao> cartoes = new ArrayList<>();
        for (int i = 0; i < comando.cartoes().size(); i++) {
            NovoCartao novo = comando.cartoes().get(i);
            Bandeira bandeira = bandeirasPorId.get(novo.bandeiraId());
            if (bandeira == null) {
                throw new IllegalStateException("Bandeira validada não encontrada.");
            }
            cartoes.add(new Cartao(null, Telefone.somenteDigitos(novo.numero()), novo.nomeImpresso().strip(),
                    bandeira, novo.validade(), i == 0));
        }
        return cartoes;
    }

    private static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
