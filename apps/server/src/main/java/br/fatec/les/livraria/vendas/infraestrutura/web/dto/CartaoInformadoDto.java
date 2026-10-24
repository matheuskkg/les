package br.fatec.les.livraria.vendas.infraestrutura.web.dto;

import java.time.YearMonth;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;

/** Cartão novo informado na compra (RF-VEN-006, RN-CLI-004, DEC-06), sem o código de segurança (DEC-10). */
public record CartaoInformadoDto(String numero, String nomeImpresso, Long bandeiraId, YearMonth validade) {

    public NovoCartao paraNovoCartao() {
        return new NovoCartao(numero, nomeImpresso, bandeiraId, validade);
    }
}
