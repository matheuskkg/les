package br.fatec.les.livraria.compartilhado.infraestrutura.e2e;

import br.fatec.les.livraria.compartilhado.infraestrutura.seed.ExecutorDeSeeds;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

/**
 * Volta o banco ao estado do seed (DEC-09): apaga os dados de todas as tabelas, inclusive as {@code _aud} do Envers e
 * {@code seed_execucao}, e reexecuta os seeds. Usa comandos do H2.
 */
@Component
@Profile("e2e")
@RequiredArgsConstructor
public class LimpezaDoBanco {

    private final DataSource dataSource;
    private final ExecutorDeSeeds seeds;

    public synchronized void restaurarSeed() throws IOException {
        JdbcClient jdbc = JdbcClient.create(dataSource);
        List<String> tabelas =
                jdbc.sql(
                                """
                                select table_name from information_schema.tables
                                where table_schema = 'public' and table_type = 'BASE TABLE'\
                                """)
                        .query(String.class)
                        .list();
        jdbc.sql("set referential_integrity false").update();
        try {
            tabelas.forEach(tabela -> jdbc.sql("truncate table \"" + tabela + "\" restart identity").update());
        } finally {
            jdbc.sql("set referential_integrity true").update();
        }
        seeds.executar();
    }
}
