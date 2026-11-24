package ru.prodflow;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class ProdFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProdFlowApplication.class, args);
    }
}