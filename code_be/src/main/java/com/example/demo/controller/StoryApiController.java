package com.example.demo.controller;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
