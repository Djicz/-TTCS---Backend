package com.example.demo.controller;

import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryApiController {
    private final StoryService storyService;

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementView(@PathVariable Long id) {
        storyService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/nominate")
    public ResponseEntity<Long> nominateStory(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        try {
            Long newNominations = storyService.nominateStory(id, authentication.getName());
            return ResponseEntity.ok(newNominations);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
