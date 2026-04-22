package com.example.demo.controller;
import com.example.demo.service.BookshelfService;
import com.example.demo.service.ReadingProgressService;
import com.example.demo.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BookshelfController {
    private final BookshelfService bookshelfService;
    private final ReadingProgressService readingProgressService;
    private final StoryRepository storyRepository;

    @GetMapping("/user/bookshelf") //lay bookshelf
    public String showBookshelf(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("bookshelf", bookshelfService.getUserBookshelf(userDetails.getUsername()));
        model.addAttribute("progressList", readingProgressService.getUserProgressList(userDetails.getUsername()));
        model.addAttribute("username", userDetails.getUsername());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "user/bookshelf";
    }

    @PostMapping("/bookshelf/add") //them truyen
    public String addToBookshelf(@RequestParam Long storyId, @AuthenticationPrincipal UserDetails userDetails) {
        bookshelfService.addToBookshelf(userDetails.getUsername(), storyId);
        // storyRepository required just for the slug in redirect
        String slug = storyRepository.findById(storyId).orElseThrow().getSlug();
        return "redirect:/story/" + slug;
    }
}
