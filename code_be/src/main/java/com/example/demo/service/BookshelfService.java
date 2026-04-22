package com.example.demo.service;

import com.example.demo.entity.Bookshelf;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.BookshelfRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookshelfService {
    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;

    public List<Bookshelf> getUserBookshelf(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return bookshelfRepository.findByUserIdOrderByAddedAtDesc(user.getId());
    }

    @Transactional
    public void addToBookshelf(String username, Long storyId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Story story = storyRepository.findById(storyId).orElseThrow();
        boolean exists = bookshelfRepository.findByUserIdAndStoryId(user.getId(), story.getId()).isPresent();
        if (!exists) {
            bookshelfRepository.save(Bookshelf.builder().user(user).story(story).notifyOnNewChapter(true).build());
        }
    }
}
