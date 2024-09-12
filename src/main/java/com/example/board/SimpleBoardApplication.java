package com.example.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SimpleBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleBoardApplication.class, args);
    }

}
