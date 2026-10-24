package br.fatec.les.livraria.compartilhado.infraestrutura.seed;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.SeedExecucaoJpa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executa os scripts de seed (RNF-LIV-002) na inicialização, depois de o Hibernate gerar o schema. Cada script roda
 * uma única vez: a execução fica registrada em {@code seed_execucao} (ver {@link SeedExecucaoJpa}) junto com o
 * checksum do arquivo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutorDeSeeds implements ApplicationRunner {

    private final DataSource dataSource;
    private final PlatformTransactionManager transacoes;
    private final PropriedadesSeed propriedades;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        executar();
    }

    public void executar() throws IOException {
        if (!propriedades.habilitado()) {
            log.info("Seeds desabilitados.");
            return;
        }
        JdbcClient jdbc = JdbcClient.create(dataSource);
        TransactionTemplate transacao = new TransactionTemplate(transacoes);

        for (Resource script : scripts()) {
            String arquivo = script.getFilename();
            String checksum = checksum(script);
            Optional<String> registrado = jdbc.sql("select checksum from seed_execucao where arquivo = ?")
                    .param(arquivo)
                    .query(String.class)
                    .optional();
            if (registrado.isPresent()) {
                if (!registrado.get().equals(checksum)) {
                    log.warn("Seed {} foi alterado depois de executado e não será executado de novo; "
                            + "recrie o banco para aplicá-lo.", arquivo);
                }
                continue;
            }
            transacao.executeWithoutResult(status -> {
                ResourceDatabasePopulator populador = new ResourceDatabasePopulator(script);
                populador.setSqlScriptEncoding("UTF-8");
                DatabasePopulatorUtils.execute(populador, dataSource);
                jdbc.sql("insert into seed_execucao (arquivo, checksum, executado_em) values (?, ?, ?)")
                        .params(arquivo, checksum, LocalDateTime.now())
                        .update();
            });
            log.info("Seed {} executado.", arquivo);
        }
    }

    private List<Resource> scripts() throws IOException {
        Resource[] encontrados = new PathMatchingResourcePatternResolver().getResources(propriedades.local());
        return Arrays.stream(encontrados)
                .filter(Resource::isReadable)
                .sorted(Comparator.comparing(Resource::getFilename))
                .toList();
    }

    private static String checksum(Resource script) throws IOException {
        try (InputStream conteudo = script.getInputStream()) {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(conteudo.readAllBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
