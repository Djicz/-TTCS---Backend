package com.example.demo.service;
import com.example.demo.entity.Chapter;
import com.example.demo.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    public List<Chapter> getChaptersByStoryId(Long storyId) {
        return chapterRepository.findByStoryIdOrderByChapterNumberAsc(storyId);
    }
    public Page<Chapter> getChaptersByStoryId(Long storyId, Pageable pageable) {
        return chapterRepository.findByStoryIdOrderByChapterNumberAsc(storyId, pageable);
    }
    public Long getFirstChapterId(Long storyId) {
        Page<Chapter> firstChapterPage = chapterRepository.findByStoryIdOrderByChapterNumberAsc(storyId, PageRequest.of(0, 1));
        if (firstChapterPage.hasContent()) {
            return firstChapterPage.getContent().get(0).getId();
        }
        return null;
    }
    public Chapter getChapterById(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
    }
    @Transactional
    public Chapter saveChapter(Chapter chapter) {
        return chapterRepository.save(chapter);
    }
    @Transactional
    public void deleteChapter(Long id) {
        chapterRepository.deleteById(id);
    }
}
