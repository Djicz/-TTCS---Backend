package com.example.demo.controller.app;
import com.example.demo.entity.Chapter;
import com.example.demo.entity.ReadingProgress;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/app/api/progress")
@RequiredArgsConstructor
public class AppProgressApiController {
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final ChapterRepository chapterRepository;
    @PostMapping("/save")
    public ResponseEntity<?> saveProgress(@RequestBody ProgressRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.ok().build();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null)
            return ResponseEntity.ok().build();
        Story story = storyRepository.findById(request.getStoryId()).orElse(null);
        Chapter chapter = chapterRepository.findById(request.getChapterId()).orElse(null);
        if (story != null && chapter != null) {
            ReadingProgress progress = progressRepository.findByUserIdAndStoryId(user.getId(), story.getId())
                    .orElseGet(() -> ReadingProgress.builder().user(user).story(story).build());
            progress.setCurrentChapter(chapter);
            progress.setScrollPercentage(request.getScrollPercentage());
            progressRepository.save(progress);
        }
        return ResponseEntity.ok().build();
    }
    @Data
    static class ProgressRequest {
        private Long storyId;
        private Long chapterId;
        private Double scrollPercentage;
    }
    @GetMapping
    public ResponseEntity<?> getAllProgress(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(progressRepository.findByUserIdOrderByLastReadAtDesc(user.getId()));
    }

    @GetMapping("/{storyId}")
    public ResponseEntity<?> getStoryProgress(@PathVariable Long storyId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "User not found"));
        }

        ReadingProgress progress = progressRepository.findByUserIdAndStoryId(user.getId(), storyId).orElse(null);
        if (progress == null) {
            return ResponseEntity.ok(java.util.Map.of("message", "No progress found"));
        }
        return ResponseEntity.ok(progress);
    }
}