package com.erebelo.springloomdemo;

import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringLoomDemoApplication {

    public static void main(String[] args) {
        // Ensures validation and other localized messages are consistently in English.
        Locale.setDefault(Locale.US);

        SpringApplication.run(SpringLoomDemoApplication.class, args);
    }
}
