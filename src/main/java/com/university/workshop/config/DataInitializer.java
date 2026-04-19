package com.university.workshop.config;

import com.university.workshop.entity.User;
import com.university.workshop.enums.Role;
import com.university.workshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin user if not exists
        if (userRepository.findByEmail("admin@university.edu").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@university.edu");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin user created: admin@university.edu / admin123");
        }
    }
}
