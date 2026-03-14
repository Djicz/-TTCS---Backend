package com.example.demo.controller;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Controller
@RequiredArgsConstructor
public class BookshelfController {
    private final BookshelfRepository bookshelfRepository;
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    @GetMapping("/user/bookshelf")
    public String showBookshelf(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("bookshelf", bookshelfRepository.findByUserIdOrderByAddedAtDesc(user.getId()));
        model.addAttribute("progressList", progressRepository.findByUserIdOrderByLastReadAtDesc(user.getId()));
        return "user/bookshelf";
    }
    @PostMapping("/bookshelf/add")
    public String addToBookshelf(@RequestParam Long storyId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Story story = storyRepository.findById(storyId).orElseThrow();
        boolean exists = bookshelfRepository.findByUserIdAndStoryId(user.getId(), story.getId()).isPresent();
        if (!exists) {
            bookshelfRepository.save(Bookshelf.builder().user(user).story(story).notifyOnNewChapter(true).build());
        }
        return "redirect:/story/" + story.getSlug();
    }
}
