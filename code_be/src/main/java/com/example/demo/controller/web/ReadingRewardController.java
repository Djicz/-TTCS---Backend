package com.example.demo.controller.web;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReadingRewardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/reward")
@RequiredArgsConstructor
public class ReadingRewardController {

    private final ReadingRewardService rewardService;
    private final UserRepository userRepository;

    @PostMapping("/claim")
    public ResponseEntity<?> claim(
            @RequestParam Long storyId,
            @RequestParam Long chapterId,
            @RequestParam int seconds,
            Authentication authentication
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String result = rewardService.claimReward(user, storyId, chapterId, seconds);

        return ResponseEntity.ok(result);
    }
}