package br.fatec.les.livraria.vendas.aplicacao.porta;

import java.util.List;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.dominio.frete.VolumeDoFrete;

/**
 * Serviço de frete (RF-VEN-004). Na demonstração, um adapter fake com as regras determinísticas do DEC-05.3.
 */
public interface CalculadoraFrete {

    /**
     * @param cep     CEP de entrega, com 8 dígitos
     * @param volumes itens da compra; sem itens, não há frete
     * @return valor do frete, com 2 casas
     */
    Dinheiro calcular(String cep, List<VolumeDoFrete> volumes);
}
