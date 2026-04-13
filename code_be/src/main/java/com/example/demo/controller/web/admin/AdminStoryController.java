package com.example.demo.controller.web.admin;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin/stories")
@RequiredArgsConstructor
public class AdminStoryController {
    private final StoryService storyService;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    
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
    
//    @PostMapping("/admin/stories/{id}/lock")
//    @PreAuthorize("hasRole('ADMIN')")
//    public String lockStory(@PathVariable Long id) {
//        Story story = storyRepository.findById(id).orElseThrow();
//        // Giả sử bạn thêm trạng thái LOCKED trong StoryStatus
//        // story.setStatus(StoryStatus.PAUSED); 
//        // Hoặc thêm một boolean isLocked vào Entity Story
//        storyRepository.save(story);
//        return "redirect:/admin/scraper";
//    }
}
