package com.example.demo.controller;

import com.example.demo.entity.Chapter;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StoryUploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/uploader/stories/{storyId}/chapters")
@RequiredArgsConstructor
public class ChapterUploaderController {
    private final StoryUploaderService uploaderService;
    private final UserRepository userRepository;

    // 1. Hiển thị form thêm chương mới
    @GetMapping("/new")
    public String showAddChapterForm(@PathVariable Long storyId, 
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (!uploaderService.isOwner(storyId, user)) {
            return "redirect:/error?msg=unauthorized";
        }
        
        Chapter chapter = new Chapter();
        model.addAttribute("chapter", chapter);
        model.addAttribute("storyId", storyId);
        return "uploader/chapter-form";
    }

    // 2. Xử lý lưu chương mới
    @PostMapping("/new")
    public String processAddChapter(@PathVariable Long storyId,
                                    @ModelAttribute Chapter chapter,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes ra) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            uploaderService.addChapterByOwner(storyId, chapter, user);
            ra.addFlashAttribute("success", "Đăng chương mới thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/uploader/stories/" + storyId + "/manage"; 
    }
}