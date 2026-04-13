package com.example.demo.service;
import com.example.demo.entity.Story;
import com.example.demo.entity.Genre;
import com.example.demo.entity.User;
import com.example.demo.entity.Chapter;
import com.example.demo.entity.ReadingProgress;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.RatingRepository;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.BookshelfRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {
    private final StoryRepository storyRepository;
    private final ChapterRepository chapterRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;
    
    
    public Page<Story> getStories(Specification<Story> spec, Pageable pageable) {
        return storyRepository.findAll(spec, pageable);
    }
    public Page<Story> getStoriesByGenre(String genreSlug, Pageable pageable) {
        return storyRepository.findByGenres_Slug(genreSlug, pageable);
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
    public List<Story> getMostNominatedStories(int limit) {
        return storyRepository.findTopByNominations(PageRequest.of(0, limit));
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
    public void updateReadingProgress(String username, Chapter chapter) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ReadingProgress progress = readingProgressRepository
                .findByUserIdAndStoryId(user.getId(), chapter.getStory().getId())
                .orElse(new ReadingProgress());
        if (progress.getId() == null) {
            progress.setUser(user);
            progress.setStory(chapter.getStory());
        }
        progress.setCurrentChapter(chapter);
        readingProgressRepository.save(progress);
    }
    
    @Transactional
    public void incrementViews(Long storyId) {
        Story story = getStoryById(storyId);
        story.setViews(story.getViews() + 1);
        storyRepository.save(story);
    }
    @Transactional
    public Long nominateStory(Long storyId) {
        Story story = getStoryById(storyId);
        long current = story.getNominations() != null ? story.getNominations() : 0L;
        story.setNominations(current + 1);
        storyRepository.save(story);
        return story.getNominations();
    }
    @Transactional(readOnly = true)
    public List<Story> getRelatedStories(String currentStorySlug, int limit) {
        Story currentStory = getStoryBySlug(currentStorySlug);
        Set<Long> currentGenreIds = currentStory.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        List<Story> candidates = storyRepository.findStoriesWithCommonGenres(currentGenreIds, currentStory.getId(), PageRequest.of(0, 150));
        return candidates.stream()
                .sorted((s1, s2) -> {
                    long count1 = s1.getGenres().stream().filter(g -> currentGenreIds.contains(g.getId())).count();
                    long count2 = s2.getGenres().stream().filter(g -> currentGenreIds.contains(g.getId())).count();
                
                    if (count1 != count2) {
                        return Long.compare(count2, count1);
                    }
                    return Long.compare(s2.getNominations(), s1.getNominations());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
}
