package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    private final UserRepository userRepository;

    @PostMapping("/reading-time") //cap nhat thoi gian doc
    public ResponseEntity<Void> updateReadingTime(@RequestParam int seconds, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        if (seconds <= 0) {
            return ResponseEntity.badRequest().build();
        }
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            long actualSeconds = Math.min(seconds, 60);
            long currentSeconds = user.getReadingTimeSeconds() != null ? user.getReadingTimeSeconds() : 0L;
            user.setReadingTimeSeconds(currentSeconds + actualSeconds);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/claim-nomination-ticket") //cap nhat phieu de cu
    public ResponseEntity<Long> claimNominationTicket(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            long currentTickets = user.getNominationTickets() != null ? user.getNominationTickets() : 0L;
            user.setNominationTickets(currentTickets + 1);
            userRepository.save(user);
            return ResponseEntity.ok(user.getNominationTickets());
        }
        return ResponseEntity.notFound().build();
    }
}
