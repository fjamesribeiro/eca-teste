package br.com.prognum.gestaoobras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GestaoObrasAceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestaoObrasAceApplication.class, args);
    }
}
