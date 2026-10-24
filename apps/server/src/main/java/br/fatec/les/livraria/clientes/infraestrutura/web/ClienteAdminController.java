package br.fatec.les.livraria.clientes.infraestrutura.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ConsultarCliente;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ListarClientes;
import br.fatec.les.livraria.clientes.aplicacao.consulta.ConsultaDeClientes;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.ClienteDetalheDto;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.ClienteListadoDto;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.web.Listagem;
import lombok.RequiredArgsConstructor;

/**
 * Consulta de clientes do administrador (RF-CLI-004, RNF-CLI-005, DEC-07, DEC-10). Só o papel {@code ADMIN} acessa
 * {@code /api/admin/**}. Os filtros são opcionais e se combinam com E; valores repetidos de gênero, UF e situação se
 * combinam com OU. O código do cliente é exibido, mas não é filtro (DEC-14), e o ranking não é ordenável nem
 * exibido (DEC-15.4).
 */
@RestController
@RequestMapping("/api/admin/clientes")
@RequiredArgsConstructor
public class ClienteAdminController {

    private final ListarClientes listarClientes;
    private final ConsultarCliente consultarCliente;
    private final MapeadorDeClientes mapeador;

    @GetMapping
    public Pagina<ClienteListadoDto> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) List<Genero> genero,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNascimentoDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNascimentoAte,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) List<SituacaoConta> situacao,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) List<Uf> uf,
            @Listagem(campos = { "nome", "dataNascimento" }, padrao = "nome,asc") Paginacao paginacao) {
        ConsultaDeClientes consulta = new ConsultaDeClientes(nome, cpf, genero, dataNascimentoDe,
                dataNascimentoAte, email, telefone, situacao, cidade, uf);
        return listarClientes.executar(consulta, paginacao).map(mapeador::paraDto);
    }

    @GetMapping("/{id}")
    public ClienteDetalheDto buscar(@PathVariable Long id) {
        return mapeador.paraDto(consultarCliente.executar(id));
    }
}
