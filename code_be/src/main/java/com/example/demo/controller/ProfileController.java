package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; //kiem tra chua dang nhap -> redirect ve view login
        }
        
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        if (user != null) { //neu co user
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

    //up avatar
    @PostMapping("/profile/upload-avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file,
                               Authentication authentication) throws IOException {
        
        if (authentication == null || file.isEmpty()) {
            return "redirect:/profile";
        }

        String username = authentication.getName();
        
        //tao folder neu chua ton tai
        String uploadDir = "user-photos";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        
        // Save file
        Files.copy(file.getInputStream(), filePath);

        // Update user
        userService.updateAvatar(username, "/user-photos/" + fileName);

        return "redirect:/profile";
    }
}
