package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import java.time.YearMonth;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;

/** Mapeamento domínio ↔ tabelas {@code cliente}, {@code endereco}, {@code cartao} e {@code bandeira} (DEC-16). */
@Mapper
public interface ClienteMapper {

    ClienteJpa paraJpa(Cliente cliente);

    Cliente paraDominio(ClienteJpa cliente);

    @Mapping(target = "cliente", ignore = true)
    EnderecoJpa paraJpa(Endereco endereco);

    Endereco paraDominio(EnderecoJpa endereco);

    @Mapping(target = "cliente", ignore = true)
    CartaoJpa paraJpa(Cartao cartao);

    Cartao paraDominio(CartaoJpa cartao);

    BandeiraJpa paraJpa(Bandeira bandeira);

    Bandeira paraDominio(BandeiraJpa bandeira);

    default String paraTexto(Cpf cpf) {
        return cpf == null ? null : cpf.numero();
    }

    default Cpf paraCpf(String numero) {
        return numero == null ? null : new Cpf(numero);
    }

    default String paraTexto(YearMonth validade) {
        return validade == null ? null : validade.toString();
    }

    default YearMonth paraYearMonth(String validade) {
        return validade == null ? null : YearMonth.parse(validade);
    }
}
