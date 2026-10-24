package br.fatec.les.livraria.clientes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.TipoLogradouro;
import br.fatec.les.livraria.clientes.dominio.modelo.Uf;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/** Dados e fakes compartilhados pelos testes unitários do módulo {@code clientes}. */
public final class ClientesDeTeste {

    public static final String SENHA = "Senha@123";
    public static final String CPF = "52998224725";
    public static final String EMAIL = "ana.souza@livraria.test";
    public static final String DDD = "11";
    public static final String NUMERO = "988887001";
    public static final YearMonth VALIDADE_FUTURA = YearMonth.of(2999, 12);
    public static final YearMonth VALIDADE_VENCIDA = YearMonth.of(2000, 1);

    private ClientesDeTeste() {
    }

    public static NovoCliente padrao() {
        return new NovoCliente("Ana Souza", Genero.FEMININO, LocalDate.of(1990, 3, 14), CPF, EMAIL,
                TipoTelefone.CELULAR, DDD, NUMERO, SENHA, SENHA, List.of(endereco()), List.of(cartao()));
    }

    public static NovoCliente comSenha(String senha) {
        NovoCliente padrao = padrao();
        return new NovoCliente(padrao.nome(), padrao.genero(), padrao.dataNascimento(), padrao.cpf(),
                padrao.email(), padrao.tipoTelefone(), padrao.ddd(), padrao.numero(), senha, senha,
                padrao.enderecos(), padrao.cartoes());
    }

    public static NovoEndereco endereco() {
        return new NovoEndereco("Casa", TipoEndereco.CASA, TipoLogradouro.RUA, "Rua das Flores", "123",
                "Jardim Paulista", "01415000", "São Paulo", Uf.SP, null);
    }

    public static NovoCartao cartao() {
        return new NovoCartao("4111111111111111", "ANA SOUZA", 1L, VALIDADE_FUTURA);
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
        public final java.util.List<Usuario> salvos = new java.util.ArrayList<>();
        private long proximoId = 2;

        @Override
        public Usuario salvar(Usuario usuario) {
            Usuario salvo = new Usuario(usuario.getId() == null ? proximoId++ : usuario.getId(),
                    usuario.getEmail(), usuario.getTelefone(), usuario.getSenhaHash(), usuario.getPapel(),
                    usuario.getInativadoEm());
            salvos.add(salvo);
            return salvo;
        }

        @Override
        public java.util.Optional<Usuario> buscarPorId(Long id) {
            return salvos.stream().filter(usuario -> usuario.getId().equals(id)).findFirst();
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
        public boolean existeTelefoneEmUso(String telefone) {
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
            Cliente salvo = cliente.getId() == null
                    ? new Cliente(10000L + salvos.size(), cliente.getUsuarioId(), cliente.getNome(),
                            cliente.getGenero(), cliente.getDataNascimento(), cliente.getCpf(),
                            cliente.getEnderecos(), cliente.getCartoes())
                    : cliente;
            salvos.add(salvo);
            return salvo;
        }

        @Override
        public java.util.List<Long> buscarUsuarioIdsPorCpf(String cpf) {
            return usuariosPorCpf.getOrDefault(cpf, java.util.List.of());
        }
    }

    public static class FakeBandeiraRepositorio implements BandeiraRepositorio {

        public final java.util.Set<Long> registradas = new java.util.HashSet<>();

        public FakeBandeiraRepositorio comBandeiras(Long... ids) {
            registradas.addAll(java.util.List.of(ids));
            return this;
        }

        @Override
        public java.util.Optional<Bandeira> buscarPorId(Long id) {
            return registradas.contains(id)
                    ? java.util.Optional.of(new Bandeira(id, "Bandeira " + id))
                    : java.util.Optional.empty();
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
    public static java.util.List<Violacao> violacoes(NovoCliente alvo, Regra<? super NovoCliente>... regras) {
        Violacoes violacoes = new Violacoes();
        for (Regra<? super NovoCliente> regra : regras) {
            regra.validar(alvo, violacoes);
        }
        return violacoes.lista();
    }

    public static Usuario usuario(Long id, String email, String telefone, Papel papel) {
        return new Usuario(id, email,
                new br.fatec.les.livraria.acesso.dominio.modelo.Telefone(TipoTelefone.CELULAR,
                        telefone.substring(0, 2), telefone.substring(2)),
                "hash", papel, null);
    }
}
