package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.calculo.DigitoVerificadorLuhn;
import br.fatec.les.livraria.clientes.dominio.calculo.IdentificarBandeira;
import br.fatec.les.livraria.clientes.dominio.calculo.TamanhoDoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Número do cartão válido (RN-CLI-004, DEC-18): havendo bandeira identificada pelo número, o comprimento precisa
 * constar nos tamanhos dela e o dígito verificador precisa passar pelo Luhn. Número sem bandeira é reportado por {@link
 * BandeiraRegistrada} (RN-CLI-005), e o formato de 13 a 19 dígitos, por {@code CartaoCompleto}. A regra é pura: recebe
 * as bandeiras já carregadas pelo caso de uso.
 */
@Component
@RequiredArgsConstructor
public class NumeroDoCartaoValido {

    private static final String CODIGO = "RN-CLI-004";

    private final IdentificarBandeira identificarBandeira;
    private final TamanhoDoCartao tamanhoDoCartao;
    private final DigitoVerificadorLuhn digitoVerificadorLuhn;

    /** Valida os cartões do cadastro, numerando as mensagens como {@code Cartão N: ...}. */
    public void validar(NovoCliente alvo, Violacoes violacoes, List<Bandeira> bandeiras) {
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            validar(cartoes.get(i), violacoes, bandeiras, "Cartão " + (i + 1) + ": ");
        }
    }

    /** Valida o número de um único cartão (adição, alteração e compra). */
    public void validar(NovoCartao cartao, Violacoes violacoes, List<Bandeira> bandeiras) {
        validar(cartao, violacoes, bandeiras, "");
    }

    private void validar(NovoCartao cartao, Violacoes violacoes, List<Bandeira> bandeiras, String prefixo) {
        if (cartao == null) {
            return;
        }
        String digitos = Telefone.somenteDigitos(cartao.numero());
        // Fora de 13–19 dígitos, o formato já é reportado por CartaoCompleto.
        if (digitos.length() < 13 || digitos.length() > 19) {
            return;
        }
        Optional<Bandeira> bandeira = identificarBandeira.identificar(digitos, bandeiras);
        if (bandeira.isEmpty()) {
            return;
        }
        if (!tamanhoDoCartao.aceita(digitos, bandeira.get()) || !digitoVerificadorLuhn.valido(digitos)) {
            violacoes.adicionar(
                    CODIGO, prefixo.isEmpty() ? "Número do cartão inválido." : prefixo + "número do cartão inválido.");
        }
    }
}
