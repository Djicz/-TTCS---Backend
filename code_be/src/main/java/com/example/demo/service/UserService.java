package com.example.demo.service;

import com.example.demo.entity.Story;
import com.example.demo.entity.User;
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

        userRepository.deleteById(userId);
    }

    @Transactional
    public void updateReadingTime(String username, int seconds) {
        if (seconds <= 0) return;
        userRepository.findByUsername(username).ifPresent(user -> {
            long actualSeconds = Math.min(seconds, 60);
            long currentSeconds = user.getReadingTimeSeconds() != null ? user.getReadingTimeSeconds() : 0L;
            user.setReadingTimeSeconds(currentSeconds + actualSeconds);
            userRepository.save(user);
        });
    }

    @Transactional
    public Long claimNominationTicket(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            long currentTickets = user.getNominationTickets() != null ? user.getNominationTickets() : 0L;
            user.setNominationTickets(currentTickets + 1);
            userRepository.save(user);
            return user.getNominationTickets();
        }
        return null;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional
    public void updateAvatar(String username, String avatarUrl) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAvatar(avatarUrl);
            userRepository.save(user);
        });
    }

    public List<User> getTopUsersByReadingTime(int limit) {
        return userRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, limit, 
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "readingTimeSeconds")))
                .getContent();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public boolean toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return user.isEnabled();
    }

    @Transactional
    public void updateUserRoles(Long id, java.util.Set<com.example.demo.entity.Role> roles) {
        User user = getUserById(id);
        user.setRoles(roles);
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getTopReaders(int limit) {
        return userRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, limit, 
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "readingTimeSeconds")))
                .getContent();
    }
}
