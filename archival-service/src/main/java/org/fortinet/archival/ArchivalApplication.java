package org.fortinet.archival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "org.fortinet.archival.model")
public class ArchivalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchivalApplication.class, args);
    }

}