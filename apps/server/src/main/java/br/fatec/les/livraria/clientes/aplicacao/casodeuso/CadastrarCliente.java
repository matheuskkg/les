package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.CriarUsuario;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.NovoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.regra.BandeiraRegistrada;
import br.fatec.les.livraria.clientes.aplicacao.regra.CpfUnico;
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
import br.fatec.les.livraria.clientes.dominio.regra.EnderecoDeCobranca;
import br.fatec.les.livraria.clientes.dominio.regra.EnderecoDeEntrega;
import br.fatec.les.livraria.clientes.dominio.regra.SenhaForte;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;
import br.fatec.les.livraria.compartilhado.dominio.Email;
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
    private final EnderecoDeCobranca enderecoDeCobranca; // RN-CLI-001
    private final EnderecoDeEntrega enderecoDeEntrega; // RN-CLI-002
    private final EnderecoCompleto enderecoCompleto; // RN-CLI-003
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
        Violacoes.verificar(comando, dadosObrigatorios, enderecoDeCobranca, enderecoDeEntrega,
                enderecoCompleto, senhaForte, confirmacaoDeSenha, cpfValido, emailValido,
                cartaoCompleto, validadeDoCartao, cpfUnico, emailUnico, telefoneUnico,
                bandeiraRegistrada);

        Long usuarioId = criarUsuario.executar(new NovoUsuario(
                new Email(comando.email()).endereco(),
                comando.tipoTelefone(),
                somenteDigitos(comando.ddd()),
                somenteDigitos(comando.numero()),
                comando.senha()));

        Cliente cliente = new Cliente(null, usuarioId, comando.nome().strip(), comando.genero(),
                comando.dataNascimento(), new Cpf(comando.cpf()),
                enderecos(comando), cartoes(comando));
        return clientes.salvar(cliente).getId();
    }

    private List<Endereco> enderecos(NovoCliente comando) {
        List<Endereco> enderecos = new ArrayList<>();
        for (NovoEndereco novo : comando.enderecos()) {
            enderecos.add(new Endereco(null, novo.nomeCurto().strip(), novo.tipoEndereco(),
                    novo.tipoLogradouro(), novo.logradouro().strip(), novo.numero().strip(),
                    novo.bairro().strip(), somenteDigitos(novo.cep()), novo.cidade().strip(), novo.uf(),
                    Endereco.PAIS, textoOuNulo(novo.observacoes())));
        }
        return enderecos;
    }

    /**
     * Monta os cartões: o primeiro vira o preferencial (RF-CLI-007, DEC-10). A bandeira já foi validada por
     * {@code BandeiraRegistrada}.
     */
    private List<Cartao> cartoes(NovoCliente comando) {
        List<Cartao> cartoes = new ArrayList<>();
        for (int i = 0; i < comando.cartoes().size(); i++) {
            NovoCartao novo = comando.cartoes().get(i);
            Bandeira bandeira = bandeiras.buscarPorId(novo.bandeiraId())
                    .orElseThrow(() -> new IllegalStateException("Bandeira validada não encontrada."));
            cartoes.add(new Cartao(null, somenteDigitos(novo.numero()), novo.nomeImpresso().strip(),
                    bandeira, novo.validade(), i == 0));
        }
        return cartoes;
    }

    private static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }

    private static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
