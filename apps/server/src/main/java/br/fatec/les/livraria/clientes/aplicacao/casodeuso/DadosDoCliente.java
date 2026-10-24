package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.time.LocalDate;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContatoDoUsuario;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

/**
 * Dados do próprio cliente (RF-CLI-002, DEC-07): o cadastro que fica no {@code Cliente} mais o e-mail e o telefone
 * que ficam no {@code Usuario} do {@code acesso}. Endereços e cartões têm rotas próprias e não entram aqui.
 */
public record DadosDoCliente(
        Long id,
        String nome,
        Genero genero,
        LocalDate dataNascimento,
        String cpf,
        String email,
        TipoTelefone tipoTelefone,
        String ddd,
        String numero) {

    public static DadosDoCliente de(Cliente cliente, ContatoDoUsuario contato) {
        return new DadosDoCliente(
                cliente.codigo(),
                cliente.getNome(),
                cliente.getGenero(),
                cliente.getDataNascimento(),
                cliente.getCpf().numero(),
                contato.email(),
                contato.tipoTelefone(),
                contato.ddd(),
                contato.numero());
    }
}
