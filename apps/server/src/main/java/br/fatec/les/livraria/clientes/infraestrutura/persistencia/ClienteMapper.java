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

    /**
     * Leitura do cartão persistido: a bandeira vem só com id e nome, sem interpretar {@code prefixos}/{@code tamanhos}.
     * A FK já aponta para a bandeira certa e a leitura não quebra por um valor malformado no seed (DEC-18).
     */
    default Cartao paraDominio(CartaoJpa cartao) {
        if (cartao == null) {
            return null;
        }
        return new Cartao(cartao.getId(), cartao.getNumero(), cartao.getNomeImpresso(),
                paraBandeiraDoCartao(cartao.getBandeira()), paraYearMonth(cartao.getValidade()),
                cartao.isPreferencial());
    }

    /** Referência para a FK: o registro da bandeira já existe no seed e só o id é necessário (DEC-18). */
    default BandeiraJpa paraJpa(Bandeira bandeira) {
        if (bandeira == null) {
            return null;
        }
        BandeiraJpa jpa = new BandeiraJpa();
        jpa.setId(bandeira.id());
        return jpa;
    }

    /** Bandeira completa, com prefixos e tamanhos interpretados a partir da tabela do seed (RN-CLI-005, DEC-18). */
    default Bandeira paraDominio(BandeiraJpa bandeira) {
        if (bandeira == null) {
            return null;
        }
        return Bandeira.de(bandeira.getId(), bandeira.getNome(), bandeira.getPrefixos(), bandeira.getTamanhos());
    }

    private static Bandeira paraBandeiraDoCartao(BandeiraJpa bandeira) {
        if (bandeira == null) {
            return null;
        }
        return new Bandeira(bandeira.getId(), bandeira.getNome());
    }

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
