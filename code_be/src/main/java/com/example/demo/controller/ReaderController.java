package com.example.demo.controller;
import com.example.demo.entity.Chapter;
import com.example.demo.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.entity.Story;
import com.example.demo.service.ReadingProgressService;
import com.example.demo.service.StoryService;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReaderController {
    private final ChapterService chapterService;
    private final StoryService storyService;
    private final ReadingProgressService readingProgressService;

    @GetMapping("/reader/{chapterId}")
    public String readChapter(@PathVariable Long chapterId, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Chapter chapter = chapterService.getChapterById(chapterId);
            model.addAttribute("chapter", chapter);
            Story story = chapter.getStory();
            model.addAttribute("story", story);

            // Increment story views
            storyService.incrementViews(story.getId());

            // Handle prev/next navigation
            List<Chapter> chapters = chapterService.getChaptersByStoryId(story.getId());
            Long prevChapterId = null;
            Long nextChapterId = null;
            for (int i = 0; i < chapters.size(); i++) {
                if (chapters.get(i).getId().equals(chapterId)) {
                    if (i > 0) prevChapterId = chapters.get(i - 1).getId();
                    if (i < chapters.size() - 1) nextChapterId = chapters.get(i + 1).getId();
                    break;
                }
            }
            model.addAttribute("prevChapterId", prevChapterId);
            model.addAttribute("nextChapterId", nextChapterId);

            Double scrollPercentage = 0.0;
            if (userDetails != null) {
                readingProgressService.updateReadingProgress(userDetails.getUsername(), chapter);
                scrollPercentage = readingProgressService.getScrollPercentage(userDetails.getUsername(), story.getId(), chapterId);
            }
            model.addAttribute("scrollPercentage", scrollPercentage);

            if (chapter.getType() == Chapter.ChapterType.COMIC) {
                return "reader/comic-reader"; //truyen tranh (chua lam)
            }
            return "reader/text-reader"; //truyen chu
        } catch (Exception ex) {
            java.io.StringWriter sw = new java.io.StringWriter();
            ex.printStackTrace(new java.io.PrintWriter(sw));
            model.addAttribute("error", sw.toString());
            throw new RuntimeException("Reader Error Trace: " + sw.toString(), ex);
        }
    }
}
