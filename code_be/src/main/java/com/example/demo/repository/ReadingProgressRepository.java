package com.example.demo.repository;
import com.example.demo.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    Optional<ReadingProgress> findByUserIdAndStoryId(Long userId, Long storyId);
    List<ReadingProgress> findByUserIdOrderByLastReadAtDesc(Long userId);
    @Modifying
    @Query("DELETE FROM ReadingProgress rp WHERE rp.story.id = :storyId")
    void deleteByStoryId(@Param("storyId") Long storyId);
    long countByUserId(Long userId);
}
