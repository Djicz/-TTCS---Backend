package com.example.demo.controller.admin;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequestMapping("/uploader/stories")
@RequiredArgsConstructor
public class AdminStoryController {
    private final StoryService storyService;
    private final UserRepository userRepository;
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("story", new Story());
        return "admin/story-form";
    }
    @PostMapping("/new")
    public String createStory(@ModelAttribute Story story, @AuthenticationPrincipal UserDetails userDetails) {
        User uploader = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        story.setUploader(uploader);
        story.setSlug(story.getTitle().toLowerCase().replace(" ", "-"));
        storyService.createStory(story);
        return "redirect:/story/" + story.getSlug();
    }
}
