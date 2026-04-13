package com.example.demo.controller.app;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/app")
public class AppLeaderboardController {
    private final UserRepository userRepository;
    @GetMapping("/leaderboard")
    public ResponseEntity<?> showLeaderboard() {
        List<User> topUsers = userRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "readingTimeSeconds"))).getContent();
        return ResponseEntity.ok(topUsers);
    }
}
