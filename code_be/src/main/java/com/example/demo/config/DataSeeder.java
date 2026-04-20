package com.example.demo.config;
import com.example.demo.entity.Role;
import com.example.demo.entity.SystemConfig;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.SystemConfigRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_MOD");
        createRoleIfNotFound("ROLE_UPLOADER");
        createRoleIfNotFound("ROLE_MEMBER");
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@webdoctruyen.com")
                    .password(passwordEncoder.encode("admin"))
                    .displayName("Administrator")
                    .readingTimeSeconds(0L)
                    .build();
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();
            admin.getRoles().add(adminRole);
            userRepository.save(admin);
            System.out.println("Created default admin user (admin/admin)");
        }
        if (!systemConfigRepository.existsById("MAINTENANCE_MODE")) {
            systemConfigRepository.save(new SystemConfig("MAINTENANCE_MODE", "false"));
        }
        if (!systemConfigRepository.existsById("ADS_ENABLED")) {
            systemConfigRepository.save(new SystemConfig("ADS_ENABLED", "true"));
        }
    }
    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(new Role(null, name));
        }
    }
}
