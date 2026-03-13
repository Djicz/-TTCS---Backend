package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.RatingRepository;
import com.example.demo.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final StoryRepository storyRepository;
    public List<Comment> getStoryComments(Long storyId) {
        return commentRepository.findByStoryIdAndParentCommentIsNullOrderByCreatedAtDesc(storyId);
    }
    public List<Comment> getChapterComments(Long chapterId) {
        return commentRepository.findByChapterIdAndParentCommentIsNullOrderByCreatedAtDesc(chapterId);
    }
    @Transactional
    public Comment addComment(User user, Story story, Chapter chapter, Long parentId, String content) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setStory(story);
        comment.setChapter(chapter);
        comment.setContent(content);
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parent);
        }
        return commentRepository.save(comment);
    }
    @Transactional
    public Rating addRating(User user, Story story, int score, String review) {
        Rating rating = ratingRepository.findByUserIdAndStoryId(user.getId(), story.getId())
                .orElse(new Rating());
        rating.setUser(user);
        rating.setStory(story);
        rating.setScore(score);
        rating.setReview(review);
        Rating saved = ratingRepository.save(rating);
        List<Rating> allRatings = ratingRepository.findByStoryIdOrderByCreatedAtDesc(story.getId());
        double avg = allRatings.stream().mapToInt(Rating::getScore).average().orElse(0.0);
        story.setAverageRating(Math.round(avg * 10.0) / 10.0);
        story.setRatingCount(allRatings.size());
        storyRepository.save(story);
        return saved;
    }
}
