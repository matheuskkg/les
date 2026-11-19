package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.BuscarUsuariosPorContato;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.BuscarUsuariosPorSituacao;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ConsultarUsuarios;
import br.fatec.les.livraria.acesso.aplicacao.consulta.ContatoDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.CalcularRanking;
import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteListado;
import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteResumo;
import br.fatec.les.livraria.clientes.aplicacao.consulta.ConsultaDeClientes;
import br.fatec.les.livraria.clientes.aplicacao.consulta.FiltroCliente;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClientesConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.Intervalo;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import lombok.RequiredArgsConstructor;

/**
 * Consulta de clientes do administrador (RF-CLI-004, DEC-07, DEC-10). O filtro da tabela {@code cliente} é
 * combinado com E. E-mail, telefone e situação vivem no módulo {@code acesso}: este caso de uso pede os ids dos
 * usuários nas situações e nos contatos informados e os entrega à porta de consulta, que filtra
 * {@code cliente.usuario_id IN (...)}. Os contatos da página são carregados em uma única consulta em lote.
 */
@Component
@RequiredArgsConstructor
public class ListarClientes {

    private final ClientesConsultas clientes;
    private final BuscarUsuariosPorContato buscarUsuariosPorContato;
    private final BuscarUsuariosPorSituacao buscarUsuariosPorSituacao;
    private final ConsultarUsuarios consultarUsuarios;
    private final Relogio relogio;
    private final CalcularRanking calcularRanking;

    /**
     * @param consulta filtros informados pelo admin; não pode ser nula
     * @param paginacao página, tamanho e ordenação já validados; não pode ser nula
     * @return a página de clientes com e-mail, telefone e situação
     * @throws br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido se o intervalo de nascimento for
     *         invertido
     */
    @Transactional(readOnly = true)
    public Pagina<ClienteListado> executar(ConsultaDeClientes consulta, Paginacao paginacao) {
        Objects.requireNonNull(consulta, "consulta");
        Objects.requireNonNull(paginacao, "paginacao");
        Intervalo.validar("dataNascimento", consulta.dataNascimentoDe(), consulta.dataNascimentoAte());
        // Um só instante para filtrar e para exibir a situação, para que os dois concordem (DEC-10).
        LocalDateTime agora = relogio.agora();

        FiltroCliente filtro = new FiltroCliente(consulta.nome(), consulta.cpf(), consulta.genero(),
                consulta.dataNascimentoDe(), consulta.dataNascimentoAte(), consulta.cidade(), consulta.uf(),
                buscarUsuariosPorContato.executar(consulta.email(), consulta.telefone()),
                idsDaSituacao(consulta, agora));

        Pagina<ClienteResumo> pagina = clientes.listar(filtro, paginacao);
        Map<Long, ContatoDoUsuario> contatos = consultarUsuarios.executar(pagina.itens().stream()
                .map(ClienteResumo::usuarioId)
                .toList(), agora);

        Map<Long, Integer> rankings = calcularRanking.executar(pagina.itens().stream()
                .map(ClienteResumo::id)
                .toList());
        return pagina.map(resumo -> new ClienteListado(resumo,
                Objects.requireNonNull(contatos.get(resumo.usuarioId()),
                        "Contato do usuário " + resumo.usuarioId() + " não encontrado."),
                rankings.get(resumo.id())));
    }


    private Optional<Set<Long>> idsDaSituacao(ConsultaDeClientes consulta, LocalDateTime agora) {
        if (consulta.situacao().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(buscarUsuariosPorSituacao.executar(consulta.situacao(), agora));
    }
}
