package com.example.demo.service;

import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.entity.Chapter;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryUploaderService {
    private final StoryRepository storyRepository;
    private final StoryService storyService;
    private final ChapterRepository chapterRepository;

    public List<Story> getStoriesByUploader(User user) {
        // Bạn cần thêm phương thức findByUploaderId trong StoryRepository nếu chưa có
        return storyRepository.findAll().stream()
                .filter(s -> s.getUploader() != null && s.getUploader().getId().equals(user.getId()))
                .toList();
    }
    
    public boolean isOwner(Long storyId, User user) {
        Story story = storyRepository.findById(storyId).orElse(null);
        return story != null && story.getUploader() != null 
               && story.getUploader().getId().equals(user.getId());
    }
    
    @Transactional
    public void deleteByOwner(Long storyId, User user) {
        if (isOwner(storyId, user)) {
            storyService.deleteStory(storyId); 
        } else {
            throw new RuntimeException("Bạn không có quyền xóa truyện này!");
        }
    }

    @Transactional
    public Story uploadNewStory(Story story, User uploader) {
        story.setUploader(uploader);
        story.setViews(0L);
        story.setNominations(0L);
        if (story.getSlug() == null || story.getSlug().isEmpty()) {
            story.setSlug(story.getTitle().toLowerCase().replaceAll("[^a-z0-9]", "-"));
        }
        return storyRepository.save(story);
    }
    
    @Transactional
    public Chapter addChapterByOwner(Long storyId, Chapter chapter, User user) {
        if (!isOwner(storyId, user)) {
            throw new RuntimeException("Bạn không có quyền thêm chương cho truyện này!");
        }
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Truyện không tồn tại"));
        chapter.setStory(story);
    
        if (chapter.getChapterNumber() == null) {
            List<Chapter> existingChapters = chapterRepository.findByStoryIdOrderByChapterNumberAsc(storyId);
            double nextNumber = existingChapters.isEmpty() ? 1.0 : 
                                existingChapters.get(existingChapters.size() - 1).getChapterNumber() + 1.0;
            chapter.setChapterNumber(nextNumber);
        }
        return chapterRepository.save(chapter);
    }
}