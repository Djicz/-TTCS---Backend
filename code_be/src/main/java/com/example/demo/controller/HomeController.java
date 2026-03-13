package com.example.demo.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final StoryService storyService;
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                model.addAttribute("readingHistory",
                        progressRepository.findByUserIdOrderByLastReadAtDesc(user.getId()));
            }
        }
        model.addAttribute("topViewed", storyService.getTopViewedStories(20));
        model.addAttribute("recentlyUpdated", storyService.getRecentlyUpdatedStories(10));
        model.addAttribute("randomStories", storyService.getRandomStories(20));
        model.addAttribute("chatMessages", chatMessageRepository
                .findLatestMessages(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "createdAt"))));
        model.addAttribute("topReaders", userRepository.findTop10ByOrderByReadingTimeSecondsDesc());
        return "index";
    }
}
