package com.localcontrol.pos.config;

import com.localcontrol.pos.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AuthService authService;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            authService.createDefaultAdmin();
        };
    }
}
