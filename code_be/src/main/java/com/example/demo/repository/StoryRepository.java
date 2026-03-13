package com.example.demo.repository;
import com.example.demo.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
@Repository
public interface StoryRepository extends JpaRepository<Story, Long>, JpaSpecificationExecutor<Story> {
    Optional<Story> findBySlug(String slug);
    @Query("SELECT s FROM Story s ORDER BY s.views DESC")
    List<Story> findTopByViews(Pageable pageable);
    @Query("SELECT s FROM Story s ORDER BY s.updatedAt DESC")
    List<Story> findTopByUpdatedAt(Pageable pageable);
    @Query("SELECT s FROM Story s ORDER BY RAND()")
    List<Story> findRandomStories(Pageable pageable);
    @Modifying
    @Query(value = "DELETE FROM story_genres WHERE story_id = ?1", nativeQuery = true)
    void deleteStoryGenresByStoryId(Long storyId);
}
