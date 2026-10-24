package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.AlterarContato;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContatoDoUsuario;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.regra.CpfUnicoNaAlteracao;
import br.fatec.les.livraria.clientes.aplicacao.regra.DataNascimentoValida;
import br.fatec.les.livraria.clientes.aplicacao.regra.EmailUnicoNaAlteracao;
import br.fatec.les.livraria.clientes.aplicacao.regra.TelefoneUnicoNaAlteracao;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.regra.CpfValido;
import br.fatec.les.livraria.clientes.dominio.regra.DadosObrigatoriosAlteracaoCliente;
import br.fatec.les.livraria.clientes.dominio.regra.EmailValido;
import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosAlteracaoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Alteração dos dados cadastrais do próprio cliente (RF-CLI-002, RN-CLI-006, DEC-07, DEC-08, DEC-10). Executa o
 * conjunto de regras da alteração — o mesmo do cadastro, sem senha e sem endereços — acumulando todas as violações.
 * O CPF é opcional: omitido, o atual é mantido; informado, passa por formato e unicidade ignorando a própria conta.
 * E-mail e telefone são gravados no {@code Usuario} pelo caso de uso público {@link AlterarContato}, e não pela
 * persistência do {@code clientes}. Devolve os dados já atualizados.
 */
@Component
@RequiredArgsConstructor
public class AlterarDadosCliente {

    private final DadosObrigatoriosAlteracaoCliente dadosObrigatorios; // RN-CLI-006
    private final TamanhosMaximosAlteracaoCliente tamanhosMaximos; // RN-CLI-006
    private final DataNascimentoValida dataNascimentoValida; // RN-CLI-006
    private final CpfValido cpfValido; // RN-CLI-006
    private final EmailValido emailValido; // RN-CLI-006
    private final CpfUnicoNaAlteracao cpfUnico; // RF-CLI-002
    private final EmailUnicoNaAlteracao emailUnico; // RF-CLI-002
    private final TelefoneUnicoNaAlteracao telefoneUnico; // RF-CLI-002
    private final ClienteRepositorio clientes;
    private final AlterarContato alterarContato;
    private final ConsultarMeusDados consultarMeusDados;

    @Transactional
    public DadosDoCliente executar(DadosAlteradosDoCliente comando) {
        Violacoes.verificar(comando, dadosObrigatorios, tamanhosMaximos, dataNascimentoValida, cpfValido,
                emailValido, cpfUnico, emailUnico, telefoneUnico);

        Cliente cliente = clientes.buscarPorUsuarioId(comando.usuarioId())
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
        Cpf cpf = comando.cpf() == null || comando.cpf().isBlank()
                ? cliente.getCpf()
                : new Cpf(comando.cpf());
        clientes.alterarDados(cliente.getId(), comando.nome().strip(), comando.genero(),
                comando.dataNascimento(), cpf);
        alterarContato.executar(comando.usuarioId(), new ContatoDoUsuario(comando.email(), comando.tipoTelefone(),
                Telefone.somenteDigitos(comando.ddd()), Telefone.somenteDigitos(comando.numero())));
        return consultarMeusDados.executar(comando.usuarioId());
    }
}
