package br.fatec.les.livraria.vendas.dominio.frete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

/** RF-VEN-004, DEC-05.3: peso considerado, blocos de 0,5 kg e base pela região do CEP. */
class RegrasDoFreteTest {

    @Test
    void rfVen004_pesoRealMaiorQueOCubicoVezesAQuantidade() {
        VolumeDoFrete volume = volume("0.400", "20", "15", "6", 2); // cúbico 0,3 kg

        assertThat(PesoConsiderado.de(volume)).isEqualByComparingTo("0.8");
    }

    @Test
    void rfVen004_pesoCubicoMaiorQueOReal() {
        VolumeDoFrete volume = volume("0.100", "30", "20", "7", 3); // cúbico 0,7 kg

        assertThat(PesoConsiderado.de(volume)).isEqualByComparingTo("2.1");
    }

    @Test
    void rfVen004_pesoCubicoComDizimaNaoFalha() {
        VolumeDoFrete volume = volume("0.001", "1", "1", "1", 1); // 1/6000

        assertThat(PesoConsiderado.de(volume)).isEqualByComparingTo("0.0010000000");
        assertThat(PesoConsiderado.de(volume("0", "7", "1", "1", 1))).isEqualByComparingTo("0.0011666667");
    }

    @ParameterizedTest
    @CsvSource({"0.001, 1", "0.5, 1", "0.501, 2", "1.0, 2", "1.01, 3", "0, 1"})
    void rfVen004_blocosIniciadosDeMeioQuiloNoMinimoUm(String peso, int blocos) {
        assertThat(BlocosDePeso.de(new BigDecimal(peso))).isEqualTo(blocos);
    }

    @ParameterizedTest
    @CsvSource({
        "01310-100, 12.00", "13000000, 12.00", "20000000, 15.00", "30160010, 15.00", "40000000, 25.00",
        "50000000, 25.00", "60000000, 30.00", "70000000, 22.00", "80020310, 18.00", "90020120, 18.00"
    })
    void rfVen004_basePeloPrimeiroDigitoDoCep(String cep, String base) {
        assertThat(BaseDaRegiao.de(cep)).isEqualTo(Dinheiro.de(base));
    }

    @Test
    void rfVen004_cepSemDigitosEhRecusado() {
        assertThatThrownBy(() -> BaseDaRegiao.de("-")).isInstanceOf(IllegalArgumentException.class);
    }

    private static VolumeDoFrete volume(
            String peso, String altura, String largura, String profundidade, int quantidade) {
        return new VolumeDoFrete(
                new BigDecimal(peso),
                new BigDecimal(altura),
                new BigDecimal(largura),
                new BigDecimal(profundidade),
                quantidade);
    }
}
