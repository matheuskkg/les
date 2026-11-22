package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.math.BigDecimal;

/** Lê {@link Dinheiro} de uma string decimal ({@code "1234.56"}); número JSON é recusado, conforme o DEC-07. */
class DesserializadorDeDinheiro extends StdDeserializer<Dinheiro> {

    DesserializadorDeDinheiro() {
        super(Dinheiro.class);
    }

    @Override
    public Dinheiro deserialize(JsonParser leitor, DeserializationContext contexto) {
        if (!leitor.hasToken(JsonToken.VALUE_STRING)) {
            return (Dinheiro) contexto.handleUnexpectedToken(Dinheiro.class, leitor);
        }
        String texto = leitor.getString().trim();
        try {
            return Dinheiro.de(new BigDecimal(texto));
        } catch (NumberFormatException e) {
            return (Dinheiro) contexto.handleWeirdStringValue(Dinheiro.class, texto, "valor monetário inválido");
        }
    }
}
