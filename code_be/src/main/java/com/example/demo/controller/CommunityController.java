package com.example.demo.controller;

import com.example.demo.entity.Chapter;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CommunityController {

    private final UserService userService;
    private final StoryService storyService;
    private final ChapterService chapterService;
    private final CommentService commentService;
    private final RatingService ratingService;

    @PostMapping("/comment/add")
    public String addComment(@RequestParam Long storyId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long parentId,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
            
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        User user = userService.getUserByUsername(userDetails.getUsername());
        Story story = storyService.getStoryById(storyId);
        Chapter chapter = chapterId != null ? chapterService.getChapterById(chapterId) : null;
        
        commentService.addComment(user, story, chapter, parentId, content);
        
        redirectAttributes.addFlashAttribute("commentSuccess", "Comment posted successfully.");
        if (chapterId != null) {
            return "redirect:/reader/" + chapterId;
        }
        return "redirect:/story/" + story.getSlug();
    }

    @PostMapping("/rating/add")
    public String addRating(@RequestParam Long storyId,
            @RequestParam int score,
            @RequestParam String review,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
            
        if (userDetails == null)
            return "redirect:/login";
            
        User user = userService.getUserByUsername(userDetails.getUsername());
        Story story = storyService.getStoryById(storyId);
        
        if (score < 1 || score > 5) {
            redirectAttributes.addFlashAttribute("error", "Score must be between 1 and 5.");
            return "redirect:/story/" + story.getSlug();
        }
        
        ratingService.addRating(user, story, score, review);
        
        redirectAttributes.addFlashAttribute("success", "Rating saved successfully.");
        return "redirect:/story/" + story.getSlug();
    }
}
