package br.fatec.les.livraria.vendas.infraestrutura.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.web.Listagem;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConfirmarEntrega;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarPedido;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.DespacharPedido;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ListarPedidos;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ConsultaDePedidos;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoDetalheDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoListadoDto;
import lombok.RequiredArgsConstructor;

/**
 * Consulta de pedidos do administrador (RF-VEN-009, RF-VEN-010, DEC-07, DEC-12). Só o papel {@code ADMIN} acessa
 * {@code /api/admin/**}. Os filtros são opcionais e se combinam com E; status repetidos combinam com OU. O código
 * do cliente filtra o histórico do cadastro (RF-CLI-005), e {@code situacao}, o status da troca mais recente do
 * pedido (DEC-12): o CPF de uma conta encerrada pode voltar em outra (DEC-10). O período
 * de {@code realizadoEm} é informado em dias, no fuso da loja, e o último dia entra inteiro. Despacho e confirmação
 * da entrega são ações em sub-rotas {@code POST} que devolvem o pedido alterado; fora do diagrama do DEC-12
 * respondem 409.
 */
@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
public class PedidoAdminController {

    private final ListarPedidos listarPedidos;
    private final ConsultarPedido consultarPedido;
    private final DespacharPedido despacharPedido;
    private final ConfirmarEntrega confirmarEntrega;

    @GetMapping
    public Pagina<PedidoListadoDto> listar(
            @RequestParam(required = false) Long numero,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) List<StatusPedido> status,
            @RequestParam(required = false) List<StatusTroca> situacao,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate realizadoEmDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate realizadoEmAte,
            @Listagem(campos = { "realizadoEm", "total", "id" }, padrao = "realizadoEm,desc") Paginacao paginacao) {
        ConsultaDePedidos consulta = new ConsultaDePedidos(numero, clienteId, status, situacao, cliente, cpf, realizadoEmDe,
                realizadoEmAte);
        return listarPedidos.executar(consulta, paginacao).map(PedidoListadoDto::de);
    }

    @GetMapping("/{id}")
    public PedidoDetalheDto buscar(@PathVariable Long id) {
        return PedidoDetalheDto.de(consultarPedido.executar(id));
    }

    @PostMapping("/{id}/despachar")
    public PedidoDetalheDto despachar(@PathVariable Long id) {
        return PedidoDetalheDto.de(despacharPedido.executar(id));
    }

    @PostMapping("/{id}/confirmar-entrega")
    public PedidoDetalheDto confirmarEntrega(@PathVariable Long id) {
        return PedidoDetalheDto.de(confirmarEntrega.executar(id));
    }
}
