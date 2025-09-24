package com.example.backend.bootstrap;

import com.example.backend.authlogin.domain.User;
import com.example.backend.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Environment env;

    public AdminSeeder(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, Environment env) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        String adminEmail = env.getProperty("ADMIN_EMAIL", "admin@example.com");
        String adminPassword = env.getProperty("ADMIN_PASSWORD", "admin1234");
        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                u -> {},
                () -> {
                    User admin = User.builder()
                            .name("Administrator")
                            .email(adminEmail)
                            .password(passwordEncoder.encode(adminPassword))
                            .providerId(null)
                            .provider(User.Provider.LOCAL)
                            .profileImageUrl(null)
                            .build();
                    admin.setRole(User.Role.ADMIN);
                    admin.setActive(true);
                    admin.setPhone("010-0000-0000");
                    admin.setAddress("Seoul");
                    try {
                        admin.setDateOfBirth(java.time.LocalDate.of(1980,1,1));
                    } catch (Exception ignored) {}
                    userRepository.save(admin);
                }
        );
    }
}
