package br.fatec.les.livraria.compartilhado.infraestrutura.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class ExecutorDeSeedsTest {

    @TempDir
    Path pasta;

    private DriverManagerDataSource dataSource;
    private JdbcClient jdbc;

    @BeforeEach
    void criarBanco() {
        dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa", "");
        jdbc = JdbcClient.create(dataSource);
    }

    @Test
    void executaOsScriptsEmOrdemERegistraCadaUm() throws IOException {
        escrever("002_dados.sql", "insert into bandeira (nome) values ('Visa');");
        escrever("001_tabela.sql", "create table bandeira (nome varchar(50));");

        executor(true).executar();

        assertThat(jdbc.sql("select nome from bandeira").query(String.class).list()).containsExactly("Visa");
        assertThat(jdbc.sql("select arquivo from seed_execucao order by arquivo").query(String.class).list())
                .containsExactly("001_tabela.sql", "002_dados.sql");
    }

    @Test
    void naoExecutaDeNovoScriptJaRegistradoMesmoSeAlterado() throws IOException {
        escrever("001_tabela.sql", "create table bandeira (nome varchar(50));");
        escrever("002_dados.sql", "insert into bandeira (nome) values ('Visa');");
        executor(true).executar();

        escrever("002_dados.sql", "insert into bandeira (nome) values ('Master');");
        escrever("003_mais_dados.sql", "insert into bandeira (nome) values ('Elo');");
        executor(true).executar();

        assertThat(jdbc.sql("select nome from bandeira order by nome").query(String.class).list())
                .containsExactly("Elo", "Visa");
    }

    @Test
    void scriptComErroNaoFicaRegistrado() throws IOException {
        escrever("001_tabela.sql", "create table bandeira (nome varchar(50));");
        escrever("002_erro.sql", "insert into tabela_inexistente values (1);");

        assertThatThrownBy(() -> executor(true).executar()).isInstanceOf(RuntimeException.class);

        assertThat(jdbc.sql("select arquivo from seed_execucao").query(String.class).list())
                .containsExactly("001_tabela.sql");
    }

    @Test
    void desabilitadoNaoFazNada() throws IOException {
        escrever("001_tabela.sql", "create table bandeira (nome varchar(50));");

        executor(false).executar();

        assertThat(jdbc.sql("select count(*) from information_schema.tables where table_name = 'bandeira'")
                .query(Integer.class).single()).isZero();
    }

    private ExecutorDeSeeds executor(boolean habilitado) {
        PropriedadesSeed propriedades = new PropriedadesSeed(habilitado, pasta.toUri() + "*.sql");
        return new ExecutorDeSeeds(dataSource, new DataSourceTransactionManager(dataSource), propriedades);
    }

    private void escrever(String arquivo, String sql) throws IOException {
        Files.writeString(pasta.resolve(arquivo), sql);
    }
}
