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
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StoryService;
import java.util.List;
import java.util.Optional;
@Controller
@RequiredArgsConstructor
public class ReaderController {
    private final ChapterService chapterService;
    private final StoryService storyService;
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
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
                    //neu chua doc chap nao hoac doc chapter khac voi chap da luu thi moi cap nhat
                    if (progress.getCurrentChapter() == null
                            || !progress.getCurrentChapter().getId().equals(chapter.getId())) {
                        Long currentChapters = user.getTotalReadChapters() != null ? user.getTotalReadChapters() : 0L;
                        user.setTotalReadChapters(currentChapters + 1); //cap nhat tong chapter cho user
                        progress.setCurrentChapter(chapter); //cap nhat progress
                    }
                    progressRepository.save(progress);
                    long totalStories = progressRepository.countByUserId(user.getId());
                    user.setTotalReadStories(totalStories); //cap nhat tong story cho user
                    userRepository.save(user);
                }
            }
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
