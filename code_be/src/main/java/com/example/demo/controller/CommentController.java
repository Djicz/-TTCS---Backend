package com.example.demo.controller;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/stories/{slug}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @PostMapping
    public String postComment(@PathVariable String slug,
                              @RequestParam String content,
                              @RequestParam(required = false) Long parentId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        
        if (userDetails == null) {
            return "redirect:/login"; //neu chua co user thi quay ve dang nhap
        }

        Story story = storyRepository.findBySlug(slug) //story
                .orElseThrow(() -> new RuntimeException("Story not found"));
        User user = userRepository.findByUsername(userDetails.getUsername()) //user
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder() //tao comment
                .story(story)
                .user(user)
                .content(content)
                .build();

        if (parentId != null) {
            commentRepository.findById(parentId).ifPresent(comment::setParentComment);
        }

        commentRepository.save(comment);

        //thong bao cho uploader
        notificationService.notifyNewComment(comment);

        redirectAttributes.addFlashAttribute("successMessage", "Đã đăng bình luận!");
        return "redirect:/story/" + slug + "#comment-" + comment.getId();
    }

    @PostMapping("/{id}/delete") //xoa comment
    public String deleteComment(@PathVariable String slug,
                                @PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isMod = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MOD"));
        boolean isOwner = comment.getUser().getUsername().equals(userDetails.getUsername());

        if (isAdmin || isMod || isOwner) { //neu la admin hoac mod hoac uploader thi co quyen xoa
            commentRepository.delete(comment);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bình luận!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa bình luận này.");
        }
        return "redirect:/story/" + slug;
    }
}
