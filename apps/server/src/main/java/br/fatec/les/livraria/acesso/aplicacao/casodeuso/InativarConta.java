package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.porta.EfeitoDaInativacao;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeConflito;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import lombok.RequiredArgsConstructor;

/**
 * Inativação da conta pelo próprio cliente (RF-CLI-003, DEC-10). Grava o instante da inativação no {@code Usuario};
 * a situação da conta é calculada a partir dele. Pedidos e trocas em andamento não são tocados. O controlador encerra
 * a sessão respondendo com o cookie expirado.
 *
 * <p>Na mesma transação, os {@link EfeitoDaInativacao} dos outros módulos são aplicados: o carrinho é esvaziado e as
 * reservas, liberadas (DEC-10). Se algum falhar, a conta continua ativa.</p>
 */
@Component
@RequiredArgsConstructor
public class InativarConta {

    private final UsuarioRepositorio usuarios;
    private final Relogio relogio;
    private final SituacaoDaConta situacaoDaConta;
    private final List<EfeitoDaInativacao> efeitos;

    @Transactional
    public void executar(Long usuarioId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Usuário não encontrado."));
        LocalDateTime agora = relogio.agora();
        if (situacaoDaConta.calcular(usuario, agora) != SituacaoConta.ATIVA) {
            throw new ExcecaoDeConflito("A conta já está inativa.");
        }
        usuario.inativar(agora);
        usuarios.salvar(usuario);
        efeitos.forEach(efeito -> efeito.aplicar(usuarioId));
    }
}
