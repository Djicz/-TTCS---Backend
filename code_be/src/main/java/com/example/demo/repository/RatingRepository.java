package com.example.demo.repository;
import com.example.demo.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndStoryId(Long userId, Long storyId);
    List<Rating> findByStoryIdOrderByCreatedAtDesc(Long storyId);
    @Modifying
    @Query("DELETE FROM Rating r WHERE r.story.id = :storyId")
    void deleteByStoryId(@Param("storyId") Long storyId);
}
