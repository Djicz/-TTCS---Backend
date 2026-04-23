package com.example.demo.service;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String SIGNER_KEY = "9d6ef1f55e8a73c936a774734f861b430d6e128e4b85c557bcc4b00a9f65c0f6";
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
    public String genToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        java.util.Map<String, Object> userClaim = new java.util.HashMap<>();
        userClaim.put("id", user.getId());
        userClaim.put("username", user.getUsername());
        userClaim.put("email", user.getEmail());
        userClaim.put("displayName", user.getDisplayName());
        userClaim.put("avatar", user.getAvatar());
        userClaim.put("readingTimeSeconds", user.getReadingTimeSeconds());
        userClaim.put("totalReadStories", user.getTotalReadStories());
        userClaim.put("totalReadChapters", user.getTotalReadChapters());
        userClaim.put("roles", user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toList()));

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("ezis.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("user", userClaim)
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        }
        catch(JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
