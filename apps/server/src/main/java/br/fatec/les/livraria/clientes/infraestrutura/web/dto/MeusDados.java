package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import java.time.LocalDate;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosDoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;

/**
 * Resposta de {@code GET /api/me} e {@code PUT /api/me} (RF-CLI-002, DEC-07): os dados do próprio cliente, com o id
 * (código, RNF-CLI-005) e o telefone. Endereços e cartões têm rotas próprias e não entram aqui.
 */
public record MeusDados(
        Long id,
        String nome,
        Genero genero,
        LocalDate dataNascimento,
        String cpf,
        String email,
        TelefoneDoCadastro telefone) {

    public static MeusDados de(DadosDoCliente dados) {
        return new MeusDados(
                dados.id(),
                dados.nome(),
                dados.genero(),
                dados.dataNascimento(),
                dados.cpf(),
                dados.email(),
                new TelefoneDoCadastro(dados.tipoTelefone(), dados.ddd(), dados.numero()));
    }
}
