package dev.yerokha.smarttale;

import dev.yerokha.smarttale.entity.user.Role;
import dev.yerokha.smarttale.repository.RoleRepository;
import dev.yerokha.smarttale.util.RSAKeyProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(RSAKeyProperties.class)
public class SmartTaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTaleApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(
            RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.count() > 0) {
                return;
            }

            roleRepository.save(new Role("USER"));
            roleRepository.save(new Role("ADMIN"));
        };
    }
}
