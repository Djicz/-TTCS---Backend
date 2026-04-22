package com.example.demo.controller;

import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
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

    private final CommentService commentService;

    @PostMapping
    public String postComment(@PathVariable String slug,
                              @RequestParam String content,
                              @RequestParam(required = false) Long parentId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        
        if (userDetails == null) {
            return "redirect:/login"; //neu chua co user thi quay ve dang nhap
        }

        Comment comment = commentService.addComment(slug, content, parentId, userDetails.getUsername());

        redirectAttributes.addFlashAttribute("successMessage", "Đã đăng bình luận!");
        return "redirect:/story/" + slug + "#comment-" + comment.getId();
    }

    @PostMapping("/{id}/delete") //xoa comment
    public String deleteComment(@PathVariable String slug,
                                @PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        
        boolean deleted = commentService.deleteComment(id, userDetails);

        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bình luận!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa bình luận này.");
        }
        return "redirect:/story/" + slug;
    }
}
