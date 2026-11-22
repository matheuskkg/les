package br.fatec.les.livraria.notificacoes.aplicacao.casodeuso;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorio;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Lista o histórico de notificações do usuário (DEC-13): não lidas primeiro e, dentro de cada grupo, as mais recentes
 * antes. A ordem padrão é aplicada aqui quando o cliente não informa {@code ordem}, para que a regra valha também para
 * chamadas programáticas.
 */
@Component
@RequiredArgsConstructor
public class ListarNotificacoes {

    /**
     * Ordem padrão: não lidas primeiro ({@code lida} vale 0 para não lida), depois a mais recente e o id como
     * desempate.
     */
    public static final List<Ordem> ORDEM_PADRAO =
            List.of(
                    new Ordem("lida", Ordem.Direcao.ASC),
                    new Ordem("criadaEm", Ordem.Direcao.DESC),
                    new Ordem("id", Ordem.Direcao.DESC));

    private final NotificacaoRepositorio notificacoes;

    /**
     * @param usuarioId destinatário; obrigatório
     * @param paginacao página, tamanho e ordenação já validados; sem ordenação, usa {@link #ORDEM_PADRAO}
     */
    @Transactional(readOnly = true)
    public Pagina<Notificacao> executar(Long usuarioId, Paginacao paginacao) {
        Objects.requireNonNull(usuarioId, "usuarioId");
        Objects.requireNonNull(paginacao, "paginacao");
        return notificacoes.listar(usuarioId, comOrdemPadrao(paginacao));
    }

    private static Paginacao comOrdemPadrao(Paginacao paginacao) {
        if (!paginacao.ordem().isEmpty()) {
            return paginacao;
        }
        return new Paginacao(paginacao.pagina(), paginacao.tamanho(), ORDEM_PADRAO);
    }
}
