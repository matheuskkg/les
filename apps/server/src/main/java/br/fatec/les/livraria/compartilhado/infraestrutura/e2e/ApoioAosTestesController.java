package br.fatec.les.livraria.compartilhado.infraestrutura.e2e;

import br.fatec.les.livraria.compartilhado.aplicacao.EstadoEmMemoria;
import br.fatec.les.livraria.compartilhado.aplicacao.TarefaPorTempo;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/** Rotas de apoio aos testes E2E, existentes só no profile {@code e2e} (DEC-09). */
@RestController
@Profile("e2e")
@RequestMapping("/api/teste")
@RequiredArgsConstructor
public class ApoioAosTestesController {

    record NovoHorario(Instant agora) {}

    private final LimpezaDoBanco limpeza;
    private final RelogioAjustavel relogio;
    private final List<TarefaPorTempo> tarefas;
    private final List<EstadoEmMemoria> estados;

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset() throws IOException {
        relogio.voltarAoSistema();
        limpeza.restaurarSeed();
        estados.forEach(EstadoEmMemoria::limpar);
    }

    @PostMapping("/relogio")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void fixarRelogio(@RequestBody NovoHorario horario) {
        relogio.fixar(horario.agora());
        tarefas.forEach(TarefaPorTempo::executar);
    }
}
