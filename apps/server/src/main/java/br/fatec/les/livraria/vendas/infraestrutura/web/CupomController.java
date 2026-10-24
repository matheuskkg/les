package br.fatec.les.livraria.vendas.infraestrutura.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ListarCuponsDeTroca;
import br.fatec.les.livraria.vendas.infraestrutura.web.dto.CupomDeTrocaDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/** Cupons de troca do cliente logado (RF-VEN-007, DEC-07, DEC-11). O cliente vem da sessão. */
@RestController
@RequestMapping("/api/me/cupons-de-troca")
@RequiredArgsConstructor
public class CupomController {

    private final ListarCuponsDeTroca listarCuponsDeTroca;

    @GetMapping
    @Operation(summary = "Lista os cupons de troca disponíveis do cliente")
    public List<CupomDeTrocaDto> listar() {
        return listarCuponsDeTroca.executar().stream().map(CupomDeTrocaDto::de).toList();
    }
}
