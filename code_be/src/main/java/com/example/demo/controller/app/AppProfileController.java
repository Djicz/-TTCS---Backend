package com.example.demo.controller.app;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Optional;
@RestController
@RequestMapping("/app")
public class AppProfileController {
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/profile")
    public ResponseEntity<?> viewProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("user", user);
            response.put("totalStoriesRead",
                    user.getTotalReadStories() != null ? user.getTotalReadStories() : 0L);
            response.put("totalChaptersRead",
                    user.getTotalReadChapters() != null ? user.getTotalReadChapters() : 0L);
            long totalSecs = user.getReadingTimeSeconds() != null ? user.getReadingTimeSeconds() : 0L;
            long hours = totalSecs / 3600;
            long minutes = (totalSecs % 3600) / 60;
            long seconds = totalSecs % 60;
            String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            response.put("readingTimeFormatted", timeFormatted);
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).body(java.util.Map.of("error", "User not found"));
    }
}
