package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.porta.ClientesConsultas;
import lombok.RequiredArgsConstructor;

/**
 * Consulta pública do {@code clientes} que resolve os ids dos clientes por nome e/ou CPF (RF-VEN-009, DEC-07). A
 * consulta de pedidos do admin recebe os ids e filtra {@code pedido.cliente_id IN (...)}. O nome comparado é o
 * atual do cliente, não a cópia gravada no pedido: um pedido de quem mudou de nome aparece pelo nome novo.
 */
@Component
@RequiredArgsConstructor
public class BuscarClientesPorNomeOuCpf {

    private final ClientesConsultas clientes;

    /**
     * @param nome trecho do nome, sem diferenciar maiúsculas nem acentos; nulo ou em branco não filtra
     * @param cpf CPF com ou sem máscara; nulo ou em branco não filtra
     * @return vazio quando nenhum dos dois foi informado (sem filtro); senão os ids que atendem aos dois,
     *         possivelmente nenhum
     * @throws br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido se o CPF não tiver nenhum dígito
     *         (HTTP 400, DEC-07)
     */
    @Transactional(readOnly = true)
    public Optional<Set<Long>> executar(String nome, String cpf) {
        if (emBranco(nome) && emBranco(cpf)) {
            return Optional.empty();
        }
        return Optional.of(clientes.buscarIds(nome, cpf));
    }

    private static boolean emBranco(String valor) {
        return valor == null || valor.isBlank();
    }
}
