package edu.java3projectpetmatchapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Java3ProjectPetMatchAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(Java3ProjectPetMatchAppApplication.class, args);
    }
}