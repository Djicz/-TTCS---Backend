package com.example.demo.controller.app;
import com.example.demo.entity.Chapter;
import com.example.demo.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.entity.ReadingProgress;
import com.example.demo.entity.User;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
@RestController
@RequiredArgsConstructor
@RequestMapping("/app")
public class AppReaderController {
    private final ChapterService chapterService;
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    @GetMapping("/reader/{chapterId}")
    public ResponseEntity<?> readChapter(@PathVariable Long chapterId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Chapter chapter = chapterService.getChapterById(chapterId);
            if (userDetails != null) {
                User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
                if (user != null) {
                    Optional<ReadingProgress> existingProgressOpt = progressRepository
                            .findByUserIdAndStoryId(user.getId(), chapter.getStory().getId());
                    ReadingProgress progress;
                    if (existingProgressOpt.isEmpty()) {
                        progress = ReadingProgress.builder().user(user).story(chapter.getStory()).build();
                    } else {
                        progress = existingProgressOpt.get();
                    }
                    if (progress.getCurrentChapter() == null
                            || !progress.getCurrentChapter().getId().equals(chapter.getId())) {
                        Long currentChapters = user.getTotalReadChapters() != null ? user.getTotalReadChapters() : 0L;
                        user.setTotalReadChapters(currentChapters + 1);
                        progress.setCurrentChapter(chapter);
                    }
                    progressRepository.save(progress);
                    long totalStories = progressRepository.countByUserId(user.getId());
                    user.setTotalReadStories(totalStories);
                    userRepository.save(user);
                }
            }
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("chapter", chapter);
            response.put("story", chapter.getStory());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            java.io.StringWriter sw = new java.io.StringWriter();
            ex.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.status(500).body(java.util.Map.of("error", sw.toString()));
        }
    }
}
