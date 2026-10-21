package br.fatec.les.livraria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LivrariaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LivrariaApplication.class, args);
    }
}
