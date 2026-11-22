package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;

import java.time.YearMonth;

/**
 * Cartão novo informado na compra (RF-VEN-006, RN-CLI-004, DEC-06, DEC-18), sem o código de segurança (DEC-10) e sem a
 * bandeira, que é identificada pelo número.
 */
public record CartaoInformadoDto(String numero, String nomeImpresso, YearMonth validade) {

    public NovoCartao paraNovoCartao() {
        return new NovoCartao(numero, nomeImpresso, validade);
    }
}
