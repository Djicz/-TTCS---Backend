package com.example.demo.repository;
import com.example.demo.entity.ReadingProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    Optional<ReadingProgress> findByUserIdAndStoryId(Long userId, Long storyId);
    Page<ReadingProgress> findByUserIdOrderByLastReadAtDesc(Long userId, Pageable pageable);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ReadingProgress rp WHERE rp.story.id = :storyId")
    void deleteByStoryId(@Param("storyId") Long storyId);
    @Modifying
    @Transactional
    @Query("DELETE FROM ReadingProgress rp WHERE rp.user.id = :userId AND rp.story.id = :storyId")
    void deleteByUserIdAndStoryId(@Param("userId") Long userId, @Param("storyId") Long storyId);
    long countByUserId(Long userId);
}
