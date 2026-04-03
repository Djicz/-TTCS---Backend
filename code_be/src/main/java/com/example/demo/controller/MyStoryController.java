package com.example.demo.controller;

import com.example.demo.entity.Story;
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
@RequestMapping("/uploader")
@RequiredArgsConstructor
public class MyStoryController {
    private final StoryUploaderService uploaderService;
    private final UserRepository userRepository;

    @GetMapping("/stories") // Thay /my-stories thành /stories cho đồng bộ
    public String listMyStories(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("stories", uploaderService.getStoriesByUploader(user));
        return "uploader/story-list"; // Quy hoạch lại thư mục view: uploader/
    }

    // Form đăng truyện mới
    @GetMapping("/stories/new")
    public String showUploadForm(Model model) {
        model.addAttribute("story", new Story());
        return "uploader/story-form"; // Nên tách riêng form uploader, không dùng chung với admin
    }

    @PostMapping("/stories/new")
    public String processUpload(@ModelAttribute Story story, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        uploaderService.uploadNewStory(story, user);
        return "redirect:/uploader/stories";
    }

    // 4. Xóa truyện (Chỉ dành cho chính chủ)
    @PostMapping("/stories/delete/{id}")
    public String deleteMyStory(@PathVariable Long id, 
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes ra) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            uploaderService.deleteByOwner(id, user); // Đã có logic check chủ sở hữu trong service
            ra.addFlashAttribute("success", "Xóa truyện thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/uploader/stories";
    }
    
    // 5. Thêm chương mới (Chỉ dành cho chính chủ)
    @GetMapping("/stories/{storyId}/chapters/new")
    public String showAddChapterForm(@PathVariable Long storyId, 
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (!uploaderService.isOwner(storyId, user)) { // Bảo mật: Check quyền sở hữu
            return "redirect:/error?msg=unauthorized";
        }
        model.addAttribute("storyId", storyId);
        return "uploader/chapter-form"; // Quy hoạch về thư mục uploader/
    }
}