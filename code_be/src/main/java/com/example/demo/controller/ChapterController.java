package com.example.demo.controller;

import com.example.demo.entity.Chapter;
import com.example.demo.service.ChapterService;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;
    private final StoryService storyService;

    @GetMapping("/reader/{chapterId}")
    public String readChapter(@PathVariable Long chapterId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Chapter chapter = chapterService.getChapterById(chapterId);
        storyService.incrementViews(chapter.getStory().getId());
        if (userDetails != null) {
            storyService.updateReadingProgress(userDetails.getUsername(), chapter);
        }
        model.addAttribute("chapter", chapter);
        return "story/reader"; 
    }
}