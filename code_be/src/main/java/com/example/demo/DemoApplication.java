package com.example.demo;

import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            userRepository.findAll().forEach(user -> {
                if (!user.isEnabled()) {
                    user.setEnabled(true);
                    userRepository.save(user);
                }
            });
            System.out.println(">>> All users enabled successfully.");
        };
    }
}
