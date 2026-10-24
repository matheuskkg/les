package br.fatec.les.livraria.clientes.infraestrutura.web;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.aplicacao.consulta.ContatoDoUsuario;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteDetalhado;
import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteListado;
import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteResumo;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.BandeiraDoCartaoDto;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.CartaoDoClienteDto;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.ClienteDetalheDto;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.ClienteListadoDto;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.EnderecoDoClienteDto;

/**
 * Converte os modelos de consulta de clientes nos DTOs da API (RF-CLI-004, DEC-07, DEC-10). O telefone é exibido
 * formatado e o cartão só com os quatro últimos dígitos.
 */
@Component
public class MapeadorDeClientes {

    public ClienteListadoDto paraDto(ClienteListado listado) {
        ClienteResumo cliente = listado.cliente();
        ContatoDoUsuario contato = listado.contato();
        return new ClienteListadoDto(cliente.id(), cliente.nome(), cliente.cpf(), cliente.genero(),
                cliente.dataNascimento(), contato.email(), telefone(contato.telefone()), contato.situacao());
    }

    public ClienteDetalheDto paraDto(ClienteDetalhado detalhado) {
        Cliente cliente = detalhado.cliente();
        ContatoDoUsuario contato = detalhado.contato();
        return new ClienteDetalheDto(cliente.getId(), cliente.getNome(), cliente.getCpf().numero(),
                cliente.getGenero(), cliente.getDataNascimento(), contato.email(),
                telefone(contato.telefone()), contato.situacao(), enderecos(cliente.getEnderecos()),
                cartoes(cliente.getCartoes()));
    }

    private List<EnderecoDoClienteDto> enderecos(List<Endereco> enderecos) {
        return enderecos.stream()
                .sorted(Comparator.comparing(Endereco::id))
                .map(this::paraDto)
                .toList();
    }

    private EnderecoDoClienteDto paraDto(Endereco endereco) {
        return new EnderecoDoClienteDto(endereco.id(), endereco.nomeCurto(), endereco.tipoEndereco(),
                endereco.tipoLogradouro(), endereco.logradouro(), endereco.numero(), endereco.bairro(),
                endereco.cep(), endereco.cidade(), endereco.uf(), endereco.pais(), endereco.observacoes());
    }

    private List<CartaoDoClienteDto> cartoes(List<Cartao> cartoes) {
        return cartoes.stream()
                .sorted(Comparator.comparing(Cartao::id))
                .map(this::paraDto)
                .toList();
    }

    private CartaoDoClienteDto paraDto(Cartao cartao) {
        return new CartaoDoClienteDto(cartao.id(), cartao.numeroMascarado(), cartao.nomeImpresso(),
                paraDto(cartao.bandeira()), cartao.validade(), cartao.preferencial());
    }

    private BandeiraDoCartaoDto paraDto(Bandeira bandeira) {
        return new BandeiraDoCartaoDto(bandeira.id(), bandeira.nome());
    }

    /** Telefone para exibição: {@code (11) 98888-7001} ou {@code (11) 3333-4444}. */
    private static String telefone(Telefone telefone) {
        if (telefone == null) {
            return null;
        }
        String numero = telefone.numero();
        int corte = numero.length() - 4;
        return "(" + telefone.ddd() + ") " + numero.substring(0, corte) + "-" + numero.substring(corte);
    }
}
