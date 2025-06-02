package com.online.chargingSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChargingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargingSystemApplication.class, args);
    }

}
