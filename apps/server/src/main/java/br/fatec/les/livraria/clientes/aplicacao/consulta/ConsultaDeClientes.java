package br.fatec.les.livraria.clientes.aplicacao.consulta;

import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Filtro informado pelo administrador na consulta de clientes (RF-CLI-004, DEC-07). E-mail, telefone e situação ainda
 * não foram resolvidos para ids de usuário: o caso de uso {@code ListarClientes} consulta o módulo {@code acesso} e
 * monta o {@link FiltroCliente} da porta, que não conhece a origem desses campos (DEC-10).
 *
 * @param nome trecho do nome, sem diferenciar maiúsculas nem acentos
 * @param cpf CPF com ou sem máscara
 * @param genero gêneros desejados, combinados com OU
 * @param dataNascimentoDe data de nascimento inicial, inclusive
 * @param dataNascimentoAte data de nascimento final, inclusive
 * @param email trecho do e-mail, sem diferenciar maiúsculas
 * @param telefone telefone com ou sem máscara, comparado só pelos dígitos
 * @param situacao situações desejadas, combinadas com OU
 * @param cidade trecho da cidade de algum endereço
 * @param uf UFs de algum endereço, combinadas com OU
 */
public record ConsultaDeClientes(
        String nome,
        String cpf,
        List<Genero> genero,
        LocalDate dataNascimentoDe,
        LocalDate dataNascimentoAte,
        String email,
        String telefone,
        List<SituacaoConta> situacao,
        String cidade,
        List<Uf> uf) {

    public ConsultaDeClientes {
        genero = genero == null ? List.of() : genero.stream().filter(Objects::nonNull).toList();
        situacao = situacao == null ? List.of() : situacao.stream().filter(Objects::nonNull).toList();
        uf = uf == null ? List.of() : uf.stream().filter(Objects::nonNull).toList();
    }
}
