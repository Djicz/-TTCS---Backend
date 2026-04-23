package com.example.demo.controller.app;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RequestMapping("/app")
@RestController
@RequiredArgsConstructor
public class AppAuthController {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthService authService;
    @GetMapping("/login")
    public ResponseEntity<?> showLoginForm() {
        return ResponseEntity.status(401).body(java.util.Map.of("error", "Authentication required"));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        System.out.println("Login Request Payload: " + body);
        String username = body.getOrDefault("username", body.get("email"));
        if (username == null) {
            username = body.get("account");
        }
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng cung cấp username (hoặc email) và password"));
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = userRepository.findByEmail(username).orElse(null);
        }

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User không tồn tại: " + username));
        }

        // check password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Sai password"));
        }

        // tạo token
        String token = authService.genToken(user);

        return ResponseEntity.ok(Map.of("token", token));
    }
    @GetMapping("/register")
    public ResponseEntity<?> showRegistrationForm() {
        return ResponseEntity.ok(java.util.Map.of("message", "Submit POST request to /register with username, email, password"));
    }
    @PostMapping("/register")
    public ResponseEntity<?> processRegistration(@RequestParam String username,
                                                 @RequestParam String email,
                                                 @RequestParam String password) {
        try {
            authService.registerUser(username, email, password);
            return ResponseEntity.ok(java.util.Map.of("message", "Đăng ký thành công! Vui lòng đăng nhập."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/forgot-password")
    public ResponseEntity<?> showForgotPasswordForm() {
        return ResponseEntity.ok(java.util.Map.of("message", "Submit POST request to /forgot-password with email"));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> processForgotPassword(@RequestParam String email) {
        authService.processForgotPassword(email);
        return ResponseEntity.ok(java.util.Map.of("message", "If an account exists with that email, a password reset link has been sent."));
    }
}