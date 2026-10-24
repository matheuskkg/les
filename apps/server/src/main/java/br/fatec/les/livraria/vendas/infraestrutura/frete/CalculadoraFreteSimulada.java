package br.fatec.les.livraria.vendas.infraestrutura.frete;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.CalculadoraFrete;
import br.fatec.les.livraria.vendas.dominio.frete.BaseDaRegiao;
import br.fatec.les.livraria.vendas.dominio.frete.BlocosDePeso;
import br.fatec.les.livraria.vendas.dominio.frete.PesoConsiderado;
import br.fatec.les.livraria.vendas.dominio.frete.VolumeDoFrete;

/**
 * Frete simulado (RF-VEN-004, DEC-05.3): base da região do CEP mais R$ 2,00 por bloco de 0,5 kg além do primeiro,
 * sobre a soma dos pesos considerados. Sem itens, não há frete.
 */
@Component
public class CalculadoraFreteSimulada implements CalculadoraFrete {

    private static final Dinheiro POR_BLOCO_EXTRA = Dinheiro.de("2.00");

    @Override
    public Dinheiro calcular(String cep, List<VolumeDoFrete> volumes) {
        if (volumes.isEmpty()) {
            return Dinheiro.ZERO;
        }
        BigDecimal pesoTotal = volumes.stream()
                .map(PesoConsiderado::de)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int blocos = BlocosDePeso.de(pesoTotal);
        return BaseDaRegiao.de(cep).somar(POR_BLOCO_EXTRA.multiplicar(blocos - 1));
    }
}
