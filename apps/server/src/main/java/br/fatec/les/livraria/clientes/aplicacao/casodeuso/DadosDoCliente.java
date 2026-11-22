package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.DadosDeContato;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

import java.time.LocalDate;

/**
 * Dados do próprio cliente (RF-CLI-002, DEC-07): o cadastro que fica no {@code Cliente} mais o e-mail e o telefone que
 * ficam no {@code Usuario} do {@code acesso}, e o ranking calculado na consulta (RN-CLI-007, DEC-04). Endereços e
 * cartões têm rotas próprias e não entram aqui.
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
        String numero,
        int ranking) {

    public static DadosDoCliente de(Cliente cliente, DadosDeContato contato, int ranking) {
        return new DadosDoCliente(
                cliente.codigo(),
                cliente.getNome(),
                cliente.getGenero(),
                cliente.getDataNascimento(),
                cliente.getCpf().numero(),
                contato.email(),
                contato.tipoTelefone(),
                contato.ddd(),
                contato.numero(),
                ranking);
    }
}
