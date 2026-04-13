package com.example.demo.controller.app.admin;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/app/uploader/stories")
@RequiredArgsConstructor
public class AppAdminStoryController {
    private final StoryService storyService;
    private final UserRepository userRepository;
    @GetMapping("/new")
    public ResponseEntity<?> showCreateForm() {
        return ResponseEntity.ok(java.util.Map.of("message", "Submit POST request to /uploader/stories/new with Story object"));
    }
    @PostMapping("/new")
    public ResponseEntity<?> createStory(@RequestBody Story story, @AuthenticationPrincipal UserDetails userDetails) {
        User uploader = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        story.setUploader(uploader);
        story.setSlug(story.getTitle().toLowerCase().replace(" ", "-"));
        storyService.createStory(story);
        return ResponseEntity.ok(story);
    }
}
