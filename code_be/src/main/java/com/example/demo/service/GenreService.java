package com.example.demo.service;
import com.example.demo.entity.Genre;
import com.example.demo.entity.Story;
import com.example.demo.repository.GenreRepository;
import com.example.demo.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;
    private final StoryRepository storyRepository;

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public Genre getGenreBySlug(String slug) {
        return genreRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
    }

    public Optional<Genre> findBySlug(String slug) {
        return genreRepository.findBySlug(slug);
    }

    public Genre getGenreById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found: " + id));
    }

    @Transactional
    public Genre createGenre(String name, String slug) {
        Genre genre = Genre.builder()
                .name(name)
                .slug(slug.trim().toLowerCase())
                .build();
        return genreRepository.save(genre);
    }

    @Transactional
    public void deleteGenre(Long id) {
        genreRepository.deleteById(id);
    }

    /**
     * Cập nhật danh sách thể loại của một truyện.
     * Gọi sau khi story đã được save (có ID).
     */
    @Transactional
    public void updateStoryGenres(Story story, List<Long> genreIds) {
        Set<Genre> selected = new HashSet<>();
        if (genreIds != null && !genreIds.isEmpty()) {
            selected.addAll(genreRepository.findAllById(genreIds));
        }
        story.setGenres(selected);
        storyRepository.save(story);
    }
}
