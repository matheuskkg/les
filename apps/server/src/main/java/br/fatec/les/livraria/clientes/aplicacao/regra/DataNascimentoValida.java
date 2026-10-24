package br.fatec.les.livraria.clientes.aplicacao.regra;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Data de nascimento plausível (RN-CLI-006): não pode estar no futuro nem ser anterior a mais de 130 anos, contando a
 * partir da data corrente da porta {@link Relogio}. A data ausente é reportada por {@link
 * br.fatec.les.livraria.clientes.dominio.regra.DadosObrigatoriosCliente}.
 */
@Component
@RequiredArgsConstructor
public class DataNascimentoValida implements Regra<NovoCliente> {

    private static final String CODIGO = "RN-CLI-006";
    private static final int IDADE_MAXIMA = 130;

    private final Relogio relogio;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        LocalDate dataNascimento = alvo.dataNascimento();
        if (dataNascimento == null) {
            return;
        }
        LocalDate hoje = relogio.hoje();
        if (dataNascimento.isAfter(hoje)) {
            violacoes.adicionar(CODIGO, "A data de nascimento não pode estar no futuro.");
        } else if (dataNascimento.isBefore(hoje.minusYears(IDADE_MAXIMA))) {
            violacoes.adicionar(CODIGO,
                    "A data de nascimento não pode ser anterior a mais de " + IDADE_MAXIMA + " anos.");
        }
    }
}
