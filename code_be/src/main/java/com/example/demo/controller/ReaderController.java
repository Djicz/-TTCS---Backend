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
import com.example.demo.entity.ReadingProgress;
import com.example.demo.entity.User;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
@Controller
@RequiredArgsConstructor
public class ReaderController {
    private final ChapterService chapterService;
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    @GetMapping("/reader/{chapterId}")
    public String readChapter(@PathVariable Long chapterId, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Chapter chapter = chapterService.getChapterById(chapterId);
            model.addAttribute("chapter", chapter);
            model.addAttribute("story", chapter.getStory());
            if (userDetails != null) {
                User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
                if (user != null) {
                    Optional<ReadingProgress> existingProgressOpt = progressRepository
                            .findByUserIdAndStoryId(user.getId(), chapter.getStory().getId());
                    ReadingProgress progress;
                    if (existingProgressOpt.isEmpty()) {
                        progress = ReadingProgress.builder().user(user).story(chapter.getStory()).build();
                    } else {
                        progress = existingProgressOpt.get();
                    }
                    if (progress.getCurrentChapter() == null
                            || !progress.getCurrentChapter().getId().equals(chapter.getId())) {
                        Long currentChapters = user.getTotalReadChapters() != null ? user.getTotalReadChapters() : 0L;
                        user.setTotalReadChapters(currentChapters + 1);
                        progress.setCurrentChapter(chapter);
                    }
                    progressRepository.save(progress);
                    long totalStories = progressRepository.countByUserId(user.getId());
                    user.setTotalReadStories(totalStories);
                    userRepository.save(user);
                }
            }
            if (chapter.getType() == Chapter.ChapterType.COMIC) {
                return "reader/comic-reader";
            }
            return "reader/text-reader";
        } catch (Exception ex) {
            java.io.StringWriter sw = new java.io.StringWriter();
            ex.printStackTrace(new java.io.PrintWriter(sw));
            model.addAttribute("error", sw.toString());
            throw new RuntimeException("Reader Error Trace: " + sw.toString(), ex);
        }
    }
}
