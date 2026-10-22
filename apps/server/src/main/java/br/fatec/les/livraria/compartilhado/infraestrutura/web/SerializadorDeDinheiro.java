package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/** Escreve {@link Dinheiro} como string decimal com 2 casas ({@code "1234.56"}), conforme o DEC-07. */
class SerializadorDeDinheiro extends StdSerializer<Dinheiro> {

    SerializadorDeDinheiro() {
        super(Dinheiro.class);
    }

    @Override
    public void serialize(Dinheiro dinheiro, JsonGenerator gerador, SerializationContext contexto) {
        gerador.writeString(dinheiro.toString());
    }
}
