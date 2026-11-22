package br.fatec.les.livraria.clientes;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoLogradouro;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/** Dados e fakes compartilhados pelos testes unitários do módulo {@code clientes}. */
public final class ClientesDeTeste {

    public static final String SENHA = "Senha@123";
    public static final String CPF = "52998224725";
    public static final String EMAIL = "ana.souza@livraria.test";
    public static final String DDD = "11";
    public static final String NUMERO = "988887001";
    public static final YearMonth VALIDADE_FUTURA = YearMonth.of(2999, 12);
    public static final YearMonth VALIDADE_VENCIDA = YearMonth.of(2000, 1);

    /** Bandeiras do seed 200_bandeiras.sql (RN-CLI-005, DEC-18), usadas pelos fakes dos testes. */
    public static final Bandeira VISA = Bandeira.de(1L, "Visa", "4", "13,16,19");

    public static final Bandeira MASTERCARD = Bandeira.de(2L, "Mastercard", "51-55,2221-2720", "16");
    public static final Bandeira ELO =
            Bandeira.de(
                    3L,
                    "Elo",
                    "401178,401179,431274,438935,451416,457393,457631,457632,504175,506699-506778,509000-509999,"
                            + "627780,636297,636368,650031-650033,650035-650051,650405-650439,650485-650538,"
                            + "650541-650598,650700-650718,650720-650727,650901-650978,651652-651679,655000-655019,"
                            + "655021-655058",
                    "16");
    public static final Bandeira AMERICAN_EXPRESS = Bandeira.de(4L, "American Express", "34,37", "15");
    public static final Bandeira HIPERCARD = Bandeira.de(5L, "Hipercard", "606282,3841", "16,19");

    private ClientesDeTeste() {}

    public static NovoCliente padrao() {
        return new NovoCliente(
                "Ana Souza",
                Genero.FEMININO,
                LocalDate.of(1990, 3, 14),
                CPF,
                EMAIL,
                TipoTelefone.CELULAR,
                DDD,
                NUMERO,
                SENHA,
                SENHA,
                List.of(endereco()),
                List.of(cartao()));
    }

    public static NovoCliente comSenha(String senha) {
        NovoCliente padrao = padrao();
        return new NovoCliente(
                padrao.nome(),
                padrao.genero(),
                padrao.dataNascimento(),
                padrao.cpf(),
                padrao.email(),
                padrao.tipoTelefone(),
                padrao.ddd(),
                padrao.numero(),
                senha,
                senha,
                padrao.enderecos(),
                padrao.cartoes());
    }

    public static NovoCliente comNome(String nome) {
        NovoCliente padrao = padrao();
        return new NovoCliente(
                nome,
                padrao.genero(),
                padrao.dataNascimento(),
                padrao.cpf(),
                padrao.email(),
                padrao.tipoTelefone(),
                padrao.ddd(),
                padrao.numero(),
                padrao.senha(),
                padrao.confirmacaoSenha(),
                padrao.enderecos(),
                padrao.cartoes());
    }

    public static NovoCliente comDataNascimento(LocalDate dataNascimento) {
        NovoCliente padrao = padrao();
        return new NovoCliente(
                padrao.nome(),
                padrao.genero(),
                dataNascimento,
                padrao.cpf(),
                padrao.email(),
                padrao.tipoTelefone(),
                padrao.ddd(),
                padrao.numero(),
                padrao.senha(),
                padrao.confirmacaoSenha(),
                padrao.enderecos(),
                padrao.cartoes());
    }

    public static NovoCliente comEnderecos(List<NovoEndereco> enderecos) {
        NovoCliente padrao = padrao();
        return new NovoCliente(
                padrao.nome(),
                padrao.genero(),
                padrao.dataNascimento(),
                padrao.cpf(),
                padrao.email(),
                padrao.tipoTelefone(),
                padrao.ddd(),
                padrao.numero(),
                padrao.senha(),
                padrao.confirmacaoSenha(),
                enderecos,
                padrao.cartoes());
    }

    public static NovoCliente comCartoes(List<NovoCartao> cartoes) {
        NovoCliente padrao = padrao();
        return new NovoCliente(
                padrao.nome(),
                padrao.genero(),
                padrao.dataNascimento(),
                padrao.cpf(),
                padrao.email(),
                padrao.tipoTelefone(),
                padrao.ddd(),
                padrao.numero(),
                padrao.senha(),
                padrao.confirmacaoSenha(),
                padrao.enderecos(),
                cartoes);
    }

    public static NovoEndereco endereco() {
        return new NovoEndereco(
                "Casa",
                TipoEndereco.CASA,
                TipoLogradouro.RUA,
                "Rua das Flores",
                "123",
                "Jardim Paulista",
                "01415000",
                "São Paulo",
                Uf.SP,
                null);
    }

    public static NovoCartao cartao() {
        return new NovoCartao("4111111111111111", "ANA SOUZA", VALIDADE_FUTURA);
    }

    public static List<Bandeira> bandeiras() {
        return List.of(VISA, MASTERCARD, ELO, AMERICAN_EXPRESS, HIPERCARD);
    }

    /** Comando da alteração dos dados do cliente (RF-CLI-002, DEC-10), com o usuário 2 (Ana do seed). */
    public static br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente alteracao() {
        return new br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente(
                2L,
                "Ana Souza",
                Genero.FEMININO,
                LocalDate.of(1990, 3, 14),
                CPF,
                EMAIL,
                TipoTelefone.CELULAR,
                DDD,
                NUMERO);
    }

    /** Cliente de domínio já existente, usado nos testes de consulta e alteração. */
    public static Cliente cliente(Long id, Long usuarioId) {
        return new Cliente(
                id,
                usuarioId,
                "Ana Souza",
                Genero.FEMININO,
                LocalDate.of(1990, 3, 14),
                new Cpf(CPF),
                List.of(enderecoDominio()),
                List.of());
    }

    /** Cliente existente com cartões, usado nos testes de alteração de cartão (RF-CLI-007). */
    public static Cliente clienteComCartoes(Long id, Long usuarioId, Cartao... cartoes) {
        return new Cliente(
                id,
                usuarioId,
                "Ana Souza",
                Genero.FEMININO,
                LocalDate.of(1990, 3, 14),
                new Cpf(CPF),
                List.of(enderecoDominio()),
                List.of(cartoes));
    }

    private static Endereco enderecoDominio() {
        return new Endereco(
                1L,
                "Casa",
                TipoEndereco.CASA,
                TipoLogradouro.RUA,
                "Rua das Flores",
                "123",
                "Jardim Paulista",
                "01415000",
                "São Paulo",
                Uf.SP,
                Endereco.PAIS,
                null);
    }

    /** Relógio fixo dos testes (2026-10-24T12:00). */
    public static Relogio relogio() {
        return new Relogio() {
            @Override
            public java.time.LocalDateTime agora() {
                return java.time.LocalDateTime.of(2026, 10, 24, 12, 0);
            }
        };
    }

    /** Repositório de usuários em memória: basta dizer quais e-mails, telefones e ids estão em uso. */
    public static class FakeUsuarioRepositorio implements UsuarioRepositorio {

        public final java.util.Set<String> emailsEmUso = new java.util.HashSet<>();
        public final java.util.Set<String> telefonesEmUso = new java.util.HashSet<>();
        public final java.util.Set<Long> idsEmUso = new java.util.HashSet<>();
        public final java.util.Map<br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta, java.util.Set<Long>>
                idsPorSituacao = new java.util.HashMap<>();
        public final java.util.Map<String, Long> donosDosEmails = new HashMap<>();
        public final java.util.Map<String, Long> donosDosTelefones = new HashMap<>();
        public final java.util.List<Usuario> salvos = new java.util.ArrayList<>();
        private long proximoId = 2;

        @Override
        public Usuario salvar(Usuario usuario) {
            Usuario salvo =
                    new Usuario(
                            usuario.getId() == null ? proximoId++ : usuario.getId(),
                            usuario.getEmail(),
                            usuario.getTelefone(),
                            usuario.getSenhaHash(),
                            usuario.getPapel(),
                            usuario.getInativadoEm());
            salvos.add(salvo);
            return salvo;
        }

        @Override
        public Optional<Usuario> buscarPorId(Long id) {
            return buscarSalvo(id);
        }

        private Optional<Usuario> buscarSalvo(Long id) {
            for (int i = salvos.size() - 1; i >= 0; i--) {
                if (salvos.get(i).getId().equals(id)) {
                    return Optional.of(salvos.get(i));
                }
            }
            return Optional.empty();
        }

        @Override
        public java.util.List<Usuario> buscarPorIds(java.util.Collection<Long> ids) {
            return salvos.stream().filter(usuario -> ids.contains(usuario.getId())).toList();
        }

        @Override
        public java.util.Set<Long> buscarIdsPorContato(String email, String telefone) {
            String termo = email == null || email.isBlank() ? null : email.strip().toLowerCase();
            String digitos =
                    telefone == null || telefone.isBlank()
                            ? null
                            : br.fatec.les.livraria.acesso.dominio.modelo.Telefone.somenteDigitos(telefone);
            return salvos.stream()
                    .filter(usuario -> termo == null || usuario.getEmail().toLowerCase().contains(termo))
                    .filter(usuario ->
                            digitos == null
                                    || (usuario.getTelefone() != null
                                            && usuario.getTelefone().digitos().equals(digitos)))
                    .map(Usuario::getId)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }

        @Override
        public java.util.Set<Long> listarIdsPorSituacao(
                java.util.Collection<br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta> situacoes,
                java.time.LocalDateTime agora) {
            java.util.Set<Long> ids = new java.util.HashSet<>();
            for (var situacao : situacoes) {
                ids.addAll(idsPorSituacao.getOrDefault(situacao, java.util.Set.of()));
            }
            return java.util.Set.copyOf(ids);
        }

        @Override
        public java.util.Optional<Usuario> buscarPorLogin(String login) {
            return java.util.Optional.empty();
        }

        @Override
        public boolean existeEmailEmUso(String email) {
            return emailsEmUso.contains(email);
        }

        @Override
        public boolean existeEmailEmUsoExceto(String email, Long usuarioId) {
            Long dono = donosDosEmails.get(email);
            if (dono != null) {
                return !dono.equals(usuarioId);
            }
            return emailsEmUso.contains(email);
        }

        @Override
        public boolean existeTelefoneEmUso(String telefone) {
            return telefonesEmUso.contains(telefone);
        }

        @Override
        public boolean existeTelefoneEmUsoExceto(String telefone, Long usuarioId) {
            Long dono = donosDosTelefones.get(telefone);
            if (dono != null) {
                return !dono.equals(usuarioId);
            }
            return telefonesEmUso.contains(telefone);
        }

        @Override
        public boolean existeAlgumaEmUso(java.util.Collection<Long> ids) {
            return ids.stream().anyMatch(idsEmUso::contains);
        }

        @Override
        public java.util.List<Long> listarAdministradores() {
            return java.util.List.of();
        }
    }

    public static class FakeClienteRepositorio implements ClienteRepositorio {

        public final java.util.Map<String, java.util.List<Long>> usuariosPorCpf = new java.util.HashMap<>();
        public final java.util.List<Cliente> salvos = new java.util.ArrayList<>();

        @Override
        public Cliente salvar(Cliente cliente) {
            Cliente salvo =
                    cliente.getId() == null
                            ? new Cliente(
                                    10000L + salvos.size(),
                                    cliente.getUsuarioId(),
                                    cliente.getNome(),
                                    cliente.getGenero(),
                                    cliente.getDataNascimento(),
                                    cliente.getCpf(),
                                    cliente.getEnderecos(),
                                    cliente.getCartoes())
                            : cliente;
            salvos.add(salvo);
            return salvo;
        }

        @Override
        public Optional<Cliente> buscarPorUsuarioIdParaAlterar(Long usuarioId) {
            return buscarPorUsuarioId(usuarioId);
        }

        @Override
        public Optional<Cliente> buscarPorUsuarioId(Long usuarioId) {
            for (int i = salvos.size() - 1; i >= 0; i--) {
                if (salvos.get(i).getUsuarioId().equals(usuarioId)) {
                    return Optional.of(salvos.get(i));
                }
            }
            return Optional.empty();
        }

        @Override
        public Cliente alterarDados(Long clienteId, String nome, Genero genero, LocalDate dataNascimento, Cpf cpf) {
            Cliente atual =
                    salvos.stream()
                            .filter(cliente -> cliente.getId().equals(clienteId))
                            .reduce((anterior, atualizado) -> atualizado)
                            .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
            Cliente alterado =
                    new Cliente(
                            atual.getId(),
                            atual.getUsuarioId(),
                            nome,
                            genero,
                            dataNascimento,
                            cpf,
                            atual.getEnderecos(),
                            atual.getCartoes());
            salvos.add(alterado);
            return alterado;
        }

        @Override
        public java.util.List<Long> buscarUsuarioIdsPorCpf(String cpf) {
            return usuariosPorCpf.getOrDefault(cpf, java.util.List.of());
        }

        @Override
        public java.util.Optional<Long> buscarIdPorUsuarioId(Long usuarioId) {
            return salvos.stream()
                    .filter(cliente -> cliente.getUsuarioId().equals(usuarioId))
                    .map(Cliente::getId)
                    .findFirst();
        }

        @Override
        public java.util.Optional<Long> buscarUsuarioIdPorClienteId(Long clienteId) {
            return salvos.stream()
                    .filter(cliente -> clienteId.equals(cliente.getId()))
                    .map(Cliente::getUsuarioId)
                    .findFirst();
        }
    }

    public static class FakeBandeiraRepositorio implements BandeiraRepositorio {

        public final List<Bandeira> registradas = new ArrayList<>();

        public FakeBandeiraRepositorio comBandeiras(Bandeira... bandeiras) {
            registradas.addAll(List.of(bandeiras));
            return this;
        }

        @Override
        public List<Bandeira> listar() {
            return List.copyOf(registradas);
        }
    }

    public static class FakeCodificadorDeSenha implements CodificadorDeSenha {

        public final java.util.List<String> codificadas = new java.util.ArrayList<>();

        @Override
        public String codificar(String senha) {
            codificadas.add(senha);
            return "hash:" + senha;
        }

        @Override
        public boolean confere(String senha, String senhaHash) {
            return ("hash:" + senha).equals(senhaHash);
        }
    }

    public static ContasEmUso contasEmUso(FakeUsuarioRepositorio usuarios) {
        return new ContasEmUso(usuarios);
    }

    @SafeVarargs
    public static <T> java.util.List<Violacao> violacoes(T alvo, Regra<? super T>... regras) {
        Violacoes violacoes = new Violacoes();
        for (Regra<? super T> regra : regras) {
            regra.validar(alvo, violacoes);
        }
        return violacoes.lista();
    }

    public static Usuario usuario(Long id, String email, String telefone, Papel papel) {
        return new Usuario(
                id,
                email,
                new br.fatec.les.livraria.acesso.dominio.modelo.Telefone(
                        TipoTelefone.CELULAR, telefone.substring(0, 2), telefone.substring(2)),
                "hash",
                papel,
                null);
    }
}
