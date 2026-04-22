package com.example.demo.service;

import com.example.demo.entity.Chapter;
import com.example.demo.entity.ReadingProgress;
import com.example.demo.entity.User;
import com.example.demo.entity.Story;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReadingProgressService {
    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final ChapterRepository chapterRepository;

    @Transactional
    public void updateReadingProgress(String username, Chapter chapter) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return;
        }

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
    
    @Transactional
    public void saveProgressApi(String username, Long storyId, Long chapterId, Double scrollPercentage) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return;
        
        Story story = storyRepository.findById(storyId).orElse(null);
        Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
        
        if (story != null && chapter != null) {
            ReadingProgress progress = progressRepository.findByUserIdAndStoryId(user.getId(), story.getId())
                    .orElseGet(() -> ReadingProgress.builder().user(user).story(story).build());
            progress.setCurrentChapter(chapter);
            progress.setScrollPercentage(scrollPercentage);
            progressRepository.save(progress);
        }
    }

    public java.util.List<ReadingProgress> getUserProgressList(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return progressRepository.findByUserIdOrderByLastReadAtDesc(user.getId());
    }
}
