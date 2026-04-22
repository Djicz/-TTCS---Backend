package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Comment addComment(String slug, String content, Long parentId, String username) {
        Story story = storyRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Story not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return addComment(user, story, null, parentId, content);
    }

    @Transactional
    public Comment addComment(User user, Story story, com.example.demo.entity.Chapter chapter, Long parentId, String content) {
        Comment comment = Comment.builder()
                .story(story)
                .user(user)
                .chapter(chapter)
                .content(content)
                .build();
        if (parentId != null) {
            commentRepository.findById(parentId).ifPresent(comment::setParentComment);
        }
        Comment saved = commentRepository.save(comment);
        notificationService.notifyNewComment(saved);
        return saved;
    }

    @Transactional
    public boolean deleteComment(Long commentId, UserDetails userDetails) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isMod = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MOD"));
        boolean isOwner = comment.getUser().getUsername().equals(userDetails.getUsername());

        if (isAdmin || isMod || isOwner) {
            commentRepository.delete(comment);
            return true;
        }
        return false;
    }
    
    public List<Comment> getCommentsByStoryId(Long storyId) {
        return commentRepository.findByStoryIdAndParentCommentIsNullOrderByCreatedAtDesc(storyId);
    }
}
