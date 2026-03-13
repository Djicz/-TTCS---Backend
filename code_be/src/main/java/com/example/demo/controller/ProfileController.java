package com.example.demo.controller;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Optional;
@Controller
public class ProfileController {
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            model.addAttribute("totalStoriesRead",
                    user.getTotalReadStories() != null ? user.getTotalReadStories() : 0L);
            model.addAttribute("totalChaptersRead",
                    user.getTotalReadChapters() != null ? user.getTotalReadChapters() : 0L);
            long totalSecs = user.getReadingTimeSeconds() != null ? user.getReadingTimeSeconds() : 0L;
            long hours = totalSecs / 3600;
            long minutes = (totalSecs % 3600) / 60;
            long seconds = totalSecs % 60;
            String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            model.addAttribute("readingTimeFormatted", timeFormatted);
            model.addAttribute("username", user.getUsername());
            return "user/profile";
        }
        return "redirect:/";
    }
}
