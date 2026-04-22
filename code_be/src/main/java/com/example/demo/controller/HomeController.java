package com.example.demo.controller;

import com.example.demo.service.ChatMessageService;
import com.example.demo.service.ReadingProgressService;
import com.example.demo.service.StoryService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final StoryService storyService;
    private final ReadingProgressService readingProgressService;
    private final UserService userService;
    private final ChatMessageService chatMessageService;

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
            
            model.addAttribute("readingHistory", 
                readingProgressService.getUserProgressList(userDetails.getUsername()));
        }
        
        model.addAttribute("topViewed", storyService.getTopViewedStories(20));
        model.addAttribute("recentlyUpdated", storyService.getRecentlyUpdatedStories(10));
        model.addAttribute("randomStories", storyService.getRandomStories(20));
        model.addAttribute("chatMessages", chatMessageService.getRecentCommunityMessages());
        model.addAttribute("topReaders", userService.getTopReaders(10));
        
        return "index";
    }

    @GetMapping("/top-liked")
    public String topLiked(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
        }
        // Lay danh sach top de cu but keep name topViewed for compatibility
        model.addAttribute("topViewed", storyService.getTopNominatedStories(30)); 
        return "top-liked";
    }
}
