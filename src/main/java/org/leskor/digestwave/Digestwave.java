package org.leskor.digestwave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Digestwave {

    public static void main(String[] args) {
        SpringApplication.run(Digestwave.class, args);
    }

}
