package com.example.demo.service;
import com.example.demo.entity.Story;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.RatingRepository;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.BookshelfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
public class StoryService {
    private final StoryRepository storyRepository;
    private final ChapterRepository chapterRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final BookshelfRepository bookshelfRepository;
    public Page<Story> getStories(Specification<Story> spec, Pageable pageable) {
        return storyRepository.findAll(spec, pageable);
    }
    public Story getStoryBySlug(String slug) {
        return storyRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Story not found"));
    }
    public Story getStoryById(Long id) {
        return storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found"));
    }
    public List<Story> getTopViewedStories(int limit) {
        return storyRepository.findTopByViews(PageRequest.of(0, limit));
    }
    public List<Story> getRecentlyUpdatedStories(int limit) {
        return storyRepository.findTopByUpdatedAt(PageRequest.of(0, limit));
    }
    public List<Story> getRandomStories(int limit) {
        return storyRepository.findRandomStories(PageRequest.of(0, limit));
    }
    @Transactional
    public Story createStory(Story story) {
        return storyRepository.save(story);
    }
    @Transactional
    public Story updateStory(Story story) {
        return storyRepository.save(story);
    }
    @Transactional
    public void deleteStory(Long id) {
        commentRepository.deleteRepliesByStoryId(id);
        commentRepository.deleteRootCommentsByStoryId(id);
        readingProgressRepository.deleteByStoryId(id);
        ratingRepository.deleteByStoryId(id);
        bookshelfRepository.deleteByStoryId(id);
        storyRepository.deleteStoryGenresByStoryId(id);
        chapterRepository.deleteByStoryId(id);
        storyRepository.deleteById(id);
    }
    @Transactional
    public void incrementViews(Long storyId) {
        Story story = getStoryById(storyId);
        story.setViews(story.getViews() + 1);
        storyRepository.save(story);
    }
}
