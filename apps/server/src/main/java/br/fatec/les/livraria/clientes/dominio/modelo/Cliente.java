package br.fatec.les.livraria.clientes.dominio.modelo;

import br.fatec.les.livraria.compartilhado.dominio.Cpf;

import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Cliente do e-commerce (RF-CLI-001, RN-CLI-006, DEC-10). O e-mail e o telefone ficam no {@code Usuario} do módulo
 * {@code acesso}; aqui há só o id dele (DEC-16). O código exibido é o próprio id (RNF-CLI-005).
 *
 * <p>O agregado protege os próprios invariantes:
 *
 * <ul>
 *   <li>todo cliente tem ao menos um endereço (RN-CLI-001, RN-CLI-002, DEC-10);
 *   <li>havendo cartões, exatamente um é o preferencial (RF-CLI-007, DEC-10).
 * </ul>
 *
 * <p>Alterar endereços e cartões devolve um novo {@code Cliente}, sem mudar outros dados (RNF-CLI-004).
 */
@Getter
public class Cliente {

    private final Long id;
    private final Long usuarioId;
    private final String nome;
    private final Genero genero;
    private final LocalDate dataNascimento;
    private final Cpf cpf;
    private final List<Endereco> enderecos;
    private final List<Cartao> cartoes;

    public Cliente(
            Long id,
            Long usuarioId,
            String nome,
            Genero genero,
            LocalDate dataNascimento,
            Cpf cpf,
            List<Endereco> enderecos,
            List<Cartao> cartoes) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId");
        this.nome = Objects.requireNonNull(nome, "nome");
        this.genero = Objects.requireNonNull(genero, "genero");
        this.dataNascimento = Objects.requireNonNull(dataNascimento, "dataNascimento");
        this.cpf = Objects.requireNonNull(cpf, "cpf");
        this.enderecos = List.copyOf(enderecos);
        this.cartoes = List.copyOf(cartoes);
        if (this.enderecos.isEmpty()) {
            throw new IllegalArgumentException("O cliente exige ao menos um endereço (RN-CLI-001, RN-CLI-002).");
        }
        long preferenciais = this.cartoes.stream().filter(Cartao::preferencial).count();
        if (!this.cartoes.isEmpty() && preferenciais != 1) {
            throw new IllegalArgumentException("Havendo cartões, exatamente um deve ser o preferencial (RF-CLI-007).");
        }
    }

    /** Código único do cliente (RNF-CLI-005, DEC-10). */
    public Long codigo() {
        return id;
    }

    /** Endereço do próprio cliente; ausente também cobre o endereço de outro cliente (DEC-07). */
    public Optional<Endereco> endereco(Long enderecoId) {
        return enderecos.stream().filter(endereco -> Objects.equals(endereco.id(), enderecoId)).findFirst();
    }

    /** Cartão do próprio cliente; ausente também cobre o cartão de outro cliente (DEC-07). */
    public Optional<Cartao> cartao(Long cartaoId) {
        return cartoes.stream().filter(cartao -> Objects.equals(cartao.id(), cartaoId)).findFirst();
    }

    /** Acrescenta um endereço sem tocar nos demais dados (RF-CLI-006, RNF-CLI-004). */
    public Cliente comEndereco(Endereco novo) {
        Objects.requireNonNull(novo, "novo");
        List<Endereco> lista = new ArrayList<>(enderecos);
        lista.add(novo);
        return novaLista(lista, cartoes);
    }

    /** Substitui os dados do endereço informado, mantendo o id (RF-CLI-006, RNF-CLI-004). */
    public Cliente comEnderecoAtualizado(Endereco alterado) {
        Objects.requireNonNull(alterado, "alterado");
        List<Endereco> lista = new ArrayList<>(enderecos);
        for (int i = 0; i < lista.size(); i++) {
            if (Objects.equals(lista.get(i).id(), alterado.id())) {
                lista.set(i, alterado);
                return novaLista(lista, cartoes);
            }
        }
        throw new IllegalArgumentException("Endereço não encontrado no cliente: " + alterado.id());
    }

    /**
     * Remove o endereço. O único endereço não pode ser removido: a lista vazia viola o invariante do cliente
     * (RN-CLI-001, RN-CLI-002, DEC-10) e o caso de uso responde 422 antes de chegar aqui.
     */
    public Cliente semEndereco(Long enderecoId) {
        List<Endereco> lista = new ArrayList<>(enderecos);
        if (!lista.removeIf(endereco -> Objects.equals(endereco.id(), enderecoId))) {
            throw new IllegalArgumentException("Endereço não encontrado no cliente: " + enderecoId);
        }
        return novaLista(lista, cartoes);
    }

    /**
     * Acrescenta um cartão. O primeiro cartão do cliente vira o preferencial (RF-CLI-007, DEC-10); os demais entram
     * como não preferenciais.
     */
    public Cliente comCartao(Cartao novo) {
        Objects.requireNonNull(novo, "novo");
        List<Cartao> lista = new ArrayList<>(cartoes);
        lista.add(comPreferencial(novo, lista.isEmpty()));
        return novaLista(enderecos, lista);
    }

    /** Substitui os dados do cartão, preservando a marcação de preferencial (RF-CLI-007). */
    public Cliente comCartaoAtualizado(Cartao alterado) {
        Objects.requireNonNull(alterado, "alterado");
        List<Cartao> lista = new ArrayList<>(cartoes);
        for (int i = 0; i < lista.size(); i++) {
            Cartao atual = lista.get(i);
            if (Objects.equals(atual.id(), alterado.id())) {
                lista.set(i, comPreferencial(alterado, atual.preferencial()));
                return novaLista(enderecos, lista);
            }
        }
        throw new IllegalArgumentException("Cartão não encontrado no cliente: " + alterado.id());
    }

    /** Marca o cartão informado como o único preferencial, desmarcando o anterior (RF-CLI-007, DEC-10). */
    public Cliente comCartaoPreferencial(Long cartaoId) {
        if (cartao(cartaoId).isEmpty()) {
            throw new IllegalArgumentException("Cartão não encontrado no cliente: " + cartaoId);
        }
        List<Cartao> lista = new ArrayList<>();
        for (Cartao cartao : cartoes) {
            lista.add(comPreferencial(cartao, Objects.equals(cartao.id(), cartaoId)));
        }
        return novaLista(enderecos, lista);
    }

    /**
     * Remove o cartão. Se ele era o preferencial, o cartão restante mais recente (maior id) assume a preferência
     * (RF-CLI-007, DEC-10). Sem cartões, nenhum fica preferencial.
     */
    public Cliente semCartao(Long cartaoId) {
        return semCartao(cartaoId, null);
    }

    /**
     * Remove o cartão; se ele era o preferencial, assume o cartão restante mais recente ainda válido em {@code
     * mesAtual} (ou o mais recente, se todos estiverem vencidos) (RF-CLI-007, DEC-06, DEC-10).
     */
    public Cliente semCartao(Long cartaoId, YearMonth mesAtual) {
        List<Cartao> lista = new ArrayList<>(cartoes);
        Cartao removido = null;
        for (Cartao cartao : lista) {
            if (Objects.equals(cartao.id(), cartaoId)) {
                removido = cartao;
                break;
            }
        }
        if (removido == null) {
            throw new IllegalArgumentException("Cartão não encontrado no cliente: " + cartaoId);
        }
        lista.remove(removido);
        if (removido.preferencial() && !lista.isEmpty()) {
            List<Cartao> validos =
                    mesAtual == null
                            ? lista
                            : lista.stream().filter(cartao -> !cartao.validade().isBefore(mesAtual)).toList();
            Cartao maisRecente = maisRecente(validos.isEmpty() ? lista : validos);
            lista.replaceAll(cartao -> comPreferencial(cartao, cartao == maisRecente));
        }
        return novaLista(enderecos, lista);
    }

    /** {@code true} se o cliente já tem um cartão com este número, com ou sem máscara (RN-CLI-004). */
    public boolean temCartaoComNumero(String numero) {
        String digitos = numero.replaceAll("\\D", "");
        return cartoes.stream().anyMatch(cartao -> cartao.numero().equals(digitos));
    }

    private Cliente novaLista(List<Endereco> novosEnderecos, List<Cartao> novosCartoes) {
        return new Cliente(id, usuarioId, nome, genero, dataNascimento, cpf, novosEnderecos, novosCartoes);
    }

    private static Cartao comPreferencial(Cartao cartao, boolean preferencial) {
        if (cartao.preferencial() == preferencial) {
            return cartao;
        }
        return new Cartao(
                cartao.id(),
                cartao.numero(),
                cartao.nomeImpresso(),
                cartao.bandeira(),
                cartao.validade(),
                preferencial);
    }

    /** O cartão cadastrado mais recentemente é o de maior id (DEC-14: a sequence cresce a cada cadastro). */
    private static Cartao maisRecente(List<Cartao> cartoes) {
        return cartoes.stream()
                .max(Comparator.comparing(Cartao::id, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow();
    }
}
