package com.example.demo.service;
import com.example.demo.entity.Genre;
import com.example.demo.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }
    public Genre getGenreBySlug(String slug) {
        return genreRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
    }
    @Transactional
    public Genre createGenre(String name, String slug) {
        Genre genre = Genre.builder()
                .name(name)
                .slug(slug)
                .build();
        return genreRepository.save(genre);
    }
    @Transactional
    public void deleteGenre(Long id) {
        genreRepository.deleteById(id);
    }
}
