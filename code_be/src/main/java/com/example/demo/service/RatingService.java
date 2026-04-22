package com.example.demo.service;

import com.example.demo.entity.Rating;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.RatingRepository;
import com.example.demo.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final StoryRepository storyRepository;

    @Transactional
    public Rating addRating(User user, Story story, int score, String review) {
        Rating rating = ratingRepository.findByUserIdAndStoryId(user.getId(), story.getId())
                .orElse(new Rating());
        
        rating.setUser(user);
        rating.setStory(story);
        rating.setScore(score);
        rating.setReview(review);
        
        Rating saved = ratingRepository.save(rating);
        
        // Update story average rating
        List<Rating> allRatings = ratingRepository.findByStoryIdOrderByCreatedAtDesc(story.getId());
        double avg = allRatings.stream().mapToInt(Rating::getScore).average().orElse(0.0);
        story.setAverageRating(Math.round(avg * 10.0) / 10.0);
        story.setRatingCount(allRatings.size());
        storyRepository.save(story);
        
        return saved;
    }
}
