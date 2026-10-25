package br.fatec.les.livraria.clientes.dominio.calculo;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.PrefixoDeCartao;

/**
 * Identifica a bandeira de um cartão pelo número (RN-CLI-005, DEC-18). Aceita o número com ou sem máscara e, havendo
 * mais de um prefixo compatível, vence o mais longo: um número que começa com {@code 438935} é Elo, não Visa.
 */
@Component
public class IdentificarBandeira {

    /**
     * @param numero    número do cartão, com ou sem máscara
     * @param bandeiras bandeiras registradas no seed
     * @return a bandeira cujo prefixo mais longo casa com o número, se houver
     */
    public Optional<Bandeira> identificar(String numero, List<Bandeira> bandeiras) {
        String digitos = Telefone.somenteDigitos(numero);
        if (digitos.isEmpty() || bandeiras == null) {
            return Optional.empty();
        }
        Bandeira identificada = null;
        int maiorPrefixo = 0;
        for (Bandeira bandeira : bandeiras) {
            for (PrefixoDeCartao prefixo : bandeira.prefixos()) {
                if (prefixo.tamanho() > maiorPrefixo && prefixo.casaCom(digitos)) {
                    identificada = bandeira;
                    maiorPrefixo = prefixo.tamanho();
                }
            }
        }
        return Optional.ofNullable(identificada);
    }
}
