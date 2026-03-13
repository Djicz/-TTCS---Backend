package com.example.demo.service;
import com.example.demo.entity.Chapter;
import com.example.demo.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    public List<Chapter> getChaptersByStoryId(Long storyId) {
        return chapterRepository.findByStoryIdOrderByChapterNumberAsc(storyId);
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
