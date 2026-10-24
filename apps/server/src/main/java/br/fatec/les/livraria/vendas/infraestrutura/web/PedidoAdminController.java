package br.fatec.les.livraria.vendas.infraestrutura.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.web.Listagem;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarPedido;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ListarPedidos;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ConsultaDePedidos;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoDetalheDto;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.PedidoListadoDto;
import lombok.RequiredArgsConstructor;

/**
 * Consulta de pedidos do administrador (RF-VEN-009, RF-VEN-010, DEC-07, DEC-12). Só o papel {@code ADMIN} acessa
 * {@code /api/admin/**}. Os filtros são opcionais e se combinam com E; status repetidos combinam com OU. O período
 * de {@code realizadoEm} é informado em dias, no fuso da loja, e o último dia entra inteiro.
 */
@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
public class PedidoAdminController {

    private final ListarPedidos listarPedidos;
    private final ConsultarPedido consultarPedido;

    @GetMapping
    public Pagina<PedidoListadoDto> listar(
            @RequestParam(required = false) Long numero,
            @RequestParam(required = false) List<StatusPedido> status,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate realizadoEmDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate realizadoEmAte,
            @Listagem(campos = { "realizadoEm", "total", "id" }, padrao = "realizadoEm,desc") Paginacao paginacao) {
        ConsultaDePedidos consulta = new ConsultaDePedidos(numero, status, cliente, cpf, realizadoEmDe,
                realizadoEmAte);
        return listarPedidos.executar(consulta, paginacao).map(PedidoListadoDto::de);
    }

    @GetMapping("/{id}")
    public PedidoDetalheDto buscar(@PathVariable Long id) {
        return PedidoDetalheDto.de(consultarPedido.executar(id));
    }
}
