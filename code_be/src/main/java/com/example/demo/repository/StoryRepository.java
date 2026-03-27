package com.example.demo.repository;
import com.example.demo.entity.Story;
import com.example.demo.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long>, JpaSpecificationExecutor<Story> {
    Page<Story> findByGenres_Slug(String slug, Pageable pageable);
    Optional<Story> findBySlug(String slug);
    
    @Query("SELECT s FROM Story s ORDER BY s.views DESC")
    List<Story> findTopByViews(Pageable pageable);
    
    @Query("SELECT s FROM Story s ORDER BY s.updatedAt DESC")
    List<Story> findTopByUpdatedAt(Pageable pageable);
    
    @Query("SELECT s FROM Story s ORDER BY RAND()")
    List<Story> findRandomStories(Pageable pageable);
    
    @Query("SELECT DISTINCT s FROM Story s JOIN s.genres g WHERE g.id IN :genreIds AND s.id != :currentStoryId ORDER BY s.updatedAt DESC")
    List<Story> findStoriesWithCommonGenres(@Param("genreIds") Set<Long> genreIds, @Param("currentStoryId") Long currentStoryId,Pageable pageable);
    
    @Query("SELECT s FROM Story s ORDER BY s.nominations DESC")
    List<Story> findTopByNominations(Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM story_genres WHERE story_id = ?1", nativeQuery = true)
    void deleteStoryGenresByStoryId(Long storyId);
}
