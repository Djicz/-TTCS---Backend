package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class LeaderboardController {
    private final UserService userService;

    @GetMapping("/leaderboard")
    public String showLeaderboard(Model model) { //lay danh sach user sort theo readingTime giam dan
        List<User> topUsers = userService.getTopUsersByReadingTime(10);
        model.addAttribute("topUsers", topUsers);
        return "community/leaderboard";
    }
}
