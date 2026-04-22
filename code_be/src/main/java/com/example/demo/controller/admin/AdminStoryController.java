package com.example.demo.controller.admin;

import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/stories")
@RequiredArgsConstructor
public class AdminStoryController {

    private final StoryService storyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MOD')")
    public String listStories(@RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Model model) {
        model.addAttribute("stories", storyService.getAllStoriesAdmin(keyword, PageRequest.of(page, size, Sort.by("id").descending())));
        model.addAttribute("keyword", keyword);
        return "admin/stories";
    }

    @PostMapping("/toggle-visibility/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOD')")
    public String toggleVisibility(@PathVariable Long id) {
        storyService.toggleStoryVisibility(id);
        return "redirect:/admin/stories";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOD')")
    public String deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        return "redirect:/admin/stories";
    }
}
