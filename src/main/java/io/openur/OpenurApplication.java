package io.openur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OpenurApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenurApplication.class, args);
    }

}
