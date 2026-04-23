package com.example.demo.controller.app;
import com.example.demo.entity.Bookshelf;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.BookshelfRepository;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app")
public class AppBookshelfController {
    private final BookshelfRepository bookshelfRepository;
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    @GetMapping("/user/bookshelf")
    public ResponseEntity<?> showBookshelf(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("bookshelf", bookshelfRepository.findByUserIdOrderByAddedAtDesc(user.getId()));
        response.put("progressList", progressRepository.findByUserIdOrderByLastReadAtDesc(user.getId()));
        return ResponseEntity.ok(response);
    }
    @PostMapping("/bookshelf/add")
    public ResponseEntity<?> addToBookshelf(@RequestParam Long storyId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Story story = storyRepository.findById(storyId).orElseThrow();
        boolean exists = bookshelfRepository.findByUserIdAndStoryId(user.getId(), story.getId()).isPresent();
        if (!exists) {
            bookshelfRepository.save(Bookshelf.builder().user(user).story(story).notifyOnNewChapter(true).build());
        }
        return ResponseEntity.ok(java.util.Map.of("message", "Added to bookshelf"));
    }
}