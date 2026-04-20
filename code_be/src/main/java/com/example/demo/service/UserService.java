package com.example.demo.service;

import com.example.demo.entity.Story;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final StoryService storyService;
    private final StoryRepository storyRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final BookshelfRepository bookshelfRepository;
    private final NotificationRepository notificationRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public void deleteUserWithData(Long userId) {
        // 1. Delete user's uploaded stories (this also cleans up story chapters, comments, etc.)
        List<Story> userStories = storyRepository.findByUploaderId(userId);
        for (Story story : userStories) {
            storyService.deleteStory(story.getId());
        }

        // 2. Delete user's own interactions across the platform
        commentRepository.deleteByUserId(userId);
        ratingRepository.deleteByUserId(userId);
        readingProgressRepository.deleteByUserId(userId);
        bookshelfRepository.deleteByUserId(userId);
        notificationRepository.deleteByUserId(userId);
        chatMessageRepository.deleteByUserId(userId);

        // 3. Finally, delete the user itself
        userRepository.deleteById(userId);
    }
}
