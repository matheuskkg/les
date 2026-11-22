package br.fatec.les.livraria.vendas.aplicacao;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarMedidasDosLivros;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.MedidasDoLivro;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.CalculadoraFrete;
import br.fatec.les.livraria.vendas.dominio.frete.VolumeDoFrete;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCheckout;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Frete dos itens do checkout para um CEP de entrega (RF-VEN-004, DEC-05.3), com o peso e as dimensões atuais de cada
 * livro no catálogo. O frete não é gravado no checkout (DEC-16): quem precisa dele calcula de novo.
 */
@Component
@RequiredArgsConstructor
public class CotarFrete {

    private final ConsultarMedidasDosLivros consultarMedidas;
    private final CalculadoraFrete calculadoraFrete;

    @Transactional(readOnly = true)
    public Dinheiro cotar(String cep, List<ItemCheckout> itens) {
        Map<Long, MedidasDoLivro> medidas =
                consultarMedidas.executar(itens.stream().map(ItemCheckout::livroId).toList());
        List<VolumeDoFrete> volumes = itens.stream().map(item -> volume(medidas.get(item.livroId()), item)).toList();
        return calculadoraFrete.calcular(cep, volumes);
    }

    private static VolumeDoFrete volume(MedidasDoLivro medidas, ItemCheckout item) {
        if (medidas == null) {
            throw new IllegalStateException("Livro " + item.livroId() + " não encontrado no catálogo.");
        }
        return new VolumeDoFrete(
                medidas.peso(), medidas.altura(), medidas.largura(), medidas.profundidade(), item.quantidade());
    }
}
