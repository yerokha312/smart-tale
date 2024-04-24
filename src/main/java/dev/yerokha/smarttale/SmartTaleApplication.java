package dev.yerokha.smarttale;

import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.user.Role;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.repository.OrderRepository;
import dev.yerokha.smarttale.repository.ProductRepository;
import dev.yerokha.smarttale.repository.RoleRepository;
import dev.yerokha.smarttale.repository.UserRepository;
import dev.yerokha.smarttale.util.RSAKeyProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
@EnableConfigurationProperties(RSAKeyProperties.class)
public class SmartTaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTaleApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(
            RoleRepository roleRepository, UserRepository userRepository, OrderRepository orderRepository, ProductRepository productRepository) {
        return args -> {
            if (roleRepository.count() > 0) {
                return;
            }

            Role userRole = roleRepository.save(new Role("USER"));
            roleRepository.save(new Role("ADMIN"));

            UserEntity user = new UserEntity(
                    "erbolatt@live.com",
                    Set.of(userRole));

            UserDetailsEntity details = new UserDetailsEntity(
                    "FirstName",
                    "LastName",
                    "Father",
                    "erbolatt@live.com"
            );
            user.setDetails(details);
            user.setEnabled(true);
            details.setUser(user);
            userRepository.save(user);

            List<OrderEntity> orders = new ArrayList<>();
            List<ProductEntity> products = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                OrderEntity order = new OrderEntity();
                order.setAdvertisementId(1000L);
                long minDay = LocalDateTime.of(1970, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
                long maxDay = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);

                LocalDateTime randomDate = LocalDateTime.ofEpochSecond(randomDay, 0, ZoneOffset.UTC);

                order.setPublishedAt(randomDate);
                order.setTitle("Order " + i);
                order.setDescription("Example description");
                order.setPublishedBy(details);
                orders.add(order);

                ProductEntity product = new ProductEntity();
                product.setAdvertisementId(1000L);
                long randomDay1 = ThreadLocalRandom.current().nextLong(minDay, maxDay);

                LocalDateTime randomDate1 = LocalDateTime.ofEpochSecond(randomDay1, 0, ZoneOffset.UTC);

                product.setPublishedAt(randomDate1);
                product.setTitle("Product " + i);
                product.setDescription("Example description");
                product.setPublishedBy(details);
                products.add(product);
            }

            orderRepository.saveAll(orders);
            productRepository.saveAll(products);
        };
    }
}
