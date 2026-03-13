package com.example.demo.controller;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
@Controller
@RequiredArgsConstructor
public class LeaderboardController {
    private final UserRepository userRepository;
    @GetMapping("/leaderboard")
    public String showLeaderboard(Model model) {
        List<User> topUsers = userRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "readingTimeSeconds"))).getContent();
        model.addAttribute("topUsers", topUsers);
        return "community/leaderboard";
    }
}
