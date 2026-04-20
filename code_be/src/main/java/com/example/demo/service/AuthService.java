package com.example.demo.service;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered!");
        }
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .displayName(username)
                .readingTimeSeconds(0L)
                .build();
        Role userRole = roleRepository.findByName("ROLE_MEMBER")
                .orElseGet(() -> {
                    Role newRole = new Role(null, "ROLE_MEMBER");
                    return roleRepository.save(newRole);
                });
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            System.out.println("Processing password recovery for: " + email);
        }
    }
}
