package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorio;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Lista as notificações do usuário com id maior que o {@code Last-Event-ID} da reconexão (DEC-13). O {@code
 * EventSource} reenvia esse header ao reconectar; o stream entrega as notificações persistidas nesse intervalo para que
 * nenhuma se perca enquanto a conexão estava caída.
 */
@Component
@RequiredArgsConstructor
public class ListarNotificacoesDesde {

    private final NotificacaoRepositorio notificacoes;

    /**
     * @param usuarioId destinatário; obrigatório
     * @param ultimoId último evento recebido pelo cliente; obrigatório
     * @return as notificações posteriores, em ordem crescente de id
     */
    @Transactional(readOnly = true)
    public List<Notificacao> executar(Long usuarioId, Long ultimoId) {
        Objects.requireNonNull(usuarioId, "usuarioId");
        Objects.requireNonNull(ultimoId, "ultimoId");
        return notificacoes.listarComIdMaiorQue(usuarioId, ultimoId);
    }
}
