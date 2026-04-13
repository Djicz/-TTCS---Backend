package com.example.demo.controller.app;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.service.StoryService;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.entity.User;
import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
@RequestMapping("/app")
public class AppHomeController {
    private final StoryService storyService;
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    @GetMapping("/")
    public ResponseEntity<?> index(@AuthenticationPrincipal UserDetails userDetails) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        if (userDetails != null) {
            response.put("username", userDetails.getUsername());
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
            response.put("isAdmin", isAdmin);
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                response.put("readingHistory",
                        progressRepository.findByUserIdOrderByLastReadAtDesc(user.getId()));
            }
        }
        response.put("topViewed", storyService.getTopViewedStories(20));
        response.put("recentlyUpdated", storyService.getRecentlyUpdatedStories(10));
        response.put("randomStories", storyService.getRandomStories(20));
        response.put("chatMessages", chatMessageRepository
                .findLatestMessages(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "createdAt"))));
        response.put("topReaders", userRepository.findTop10ByOrderByReadingTimeSecondsDesc());
        return ResponseEntity.ok(response);
    }
}
