package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.calculo.IdentificarBandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Bandeira registrada (RN-CLI-005, DEC-18): a bandeira do cartão é identificada pelo número na tabela do seed, e um
 * número sem bandeira correspondente é recusado. A regra é pura: recebe as bandeiras já carregadas pelo caso de uso. O
 * tamanho conforme a bandeira e o dígito verificador ficam em {@link NumeroDoCartaoValido} (RN-CLI-004).
 */
@Component
@RequiredArgsConstructor
public class BandeiraRegistrada {

    private static final String CODIGO = "RN-CLI-005";

    private final IdentificarBandeira identificarBandeira;

    /**
     * Identifica as bandeiras dos cartões do cadastro e devolve uma lista alinhada aos cartões: a posição de um cartão
     * sem bandeira fica vazia, para o caso de uso não trocar as bandeiras entre as posições.
     */
    public List<Optional<Bandeira>> identificar(NovoCliente alvo, Violacoes violacoes, List<Bandeira> bandeiras) {
        List<NovoCartao> cartoes = alvo.cartoes();
        List<Optional<Bandeira>> identificadas = new ArrayList<>();
        for (int i = 0; i < cartoes.size(); i++) {
            identificadas.add(identificar(cartoes.get(i), violacoes, bandeiras, "Cartão " + (i + 1) + ": "));
        }
        return identificadas;
    }

    /** Identifica a bandeira de um único cartão e devolve a bandeira registrada, quando encontrada. */
    public Optional<Bandeira> identificar(NovoCartao cartao, Violacoes violacoes, List<Bandeira> bandeiras) {
        return identificar(cartao, violacoes, bandeiras, "");
    }

    private Optional<Bandeira> identificar(
            NovoCartao cartao, Violacoes violacoes, List<Bandeira> bandeiras, String prefixo) {
        if (cartao == null) {
            return Optional.empty();
        }
        String digitos = Telefone.somenteDigitos(cartao.numero());
        // Fora de 13–19 dígitos, o formato já é reportado por CartaoCompleto; não há bandeira a identificar.
        if (digitos.length() < 13 || digitos.length() > 19) {
            return Optional.empty();
        }
        Optional<Bandeira> bandeira = identificarBandeira.identificar(digitos, bandeiras);
        if (bandeira.isEmpty()) {
            violacoes.adicionar(
                    CODIGO,
                    prefixo.isEmpty() ? "Bandeira do cartão não aceita." : prefixo + "bandeira do cartão não aceita.");
        }
        return bandeira;
    }
}
