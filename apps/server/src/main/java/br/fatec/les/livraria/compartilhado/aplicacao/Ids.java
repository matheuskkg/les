package br.fatec.les.livraria.compartilhado.aplicacao;

import java.util.Collection;
import java.util.Objects;

/**
 * Validação comum das consultas em lote por id (DEC-15.3). Os casos de uso exigem uma coleção não nula, sem elementos
 * nulos, e devolvem um mapa vazio sem acionar a porta quando não há nenhum id.
 */
public final class Ids {

    private Ids() {}

    /**
     * @param ids ids de uma consulta em lote; não pode ser nulo nem conter elementos nulos
     * @return {@code true} quando a coleção está vazia, caso em que a porta não deve ser acionada
     * @throws NullPointerException se a coleção ou algum elemento for nulo
     */
    public static boolean validar(Collection<Long> ids) {
        Objects.requireNonNull(ids, "ids");
        ids.forEach(id -> Objects.requireNonNull(id, "ids não pode conter null"));
        return ids.isEmpty();
    }
}
