package br.fatec.les.livraria.notificacoes.infraestrutura.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.web.Listagem;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.ContarNaoLidas;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.ListarNotificacoes;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.MarcarComoLida;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.MarcarTodasComoLidas;
import br.fatec.les.livraria.notificacoes.infraestrutura.web.dto.ContagemDeNaoLidasDto;
import br.fatec.les.livraria.notificacoes.infraestrutura.web.dto.NotificacaoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * Notificações do cliente autenticado (DEC-13, RN-VEN-014, RN-VEN-016). O id do usuário vem da sessão; nenhuma rota
 * aceita o id de outra pessoa (DEC-07).
 */
@RestController
@RequestMapping("/api/me/notificacoes")
@RequiredArgsConstructor
public class NotificacoesDoClienteController {

    private final ListarNotificacoes listarNotificacoes;
    private final ContarNaoLidas contarNaoLidas;
    private final MarcarComoLida marcarComoLida;
    private final MarcarTodasComoLidas marcarTodasComoLidas;
    private final MapeadorDeNotificacoes mapeador;

    @GetMapping
    @Operation(summary = "Lista as notificações do cliente, não lidas primeiro e mais recentes antes")
    public Pagina<NotificacaoDto> listarNotificacoesDoCliente(
            @Listagem(campos = { "id", "tipo", "criadaEm", "lidaEm", "lida" },
                    padrao = { "lida,asc", "criadaEm,desc", "id,desc" }) Paginacao paginacao,
            Authentication autenticacao) {
        return listarNotificacoes.executar(UsuarioDaRequisicao.id(autenticacao), paginacao)
                .map(mapeador::paraDto);
    }

    @GetMapping("/nao-lidas")
    @Operation(summary = "Conta as notificações não lidas do cliente")
    public ContagemDeNaoLidasDto contarNaoLidasDoCliente(Authentication autenticacao) {
        return new ContagemDeNaoLidasDto(contarNaoLidas.executar(UsuarioDaRequisicao.id(autenticacao)));
    }

    @PostMapping("/{id}/ler")
    @Operation(summary = "Marca uma notificação do cliente como lida")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificação marcada como lida"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada para o cliente")
    })
    public ResponseEntity<Void> marcarComoLidaDoCliente(@PathVariable Long id, Authentication autenticacao) {
        marcarComoLida.executar(UsuarioDaRequisicao.id(autenticacao), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ler-todas")
    @Operation(summary = "Marca todas as notificações do cliente como lidas")
    @ApiResponses(@ApiResponse(responseCode = "204", description = "Notificações marcadas como lidas"))
    public ResponseEntity<Void> marcarTodasComoLidasDoCliente(Authentication autenticacao) {
        marcarTodasComoLidas.executar(UsuarioDaRequisicao.id(autenticacao));
        return ResponseEntity.noContent().build();
    }
}
