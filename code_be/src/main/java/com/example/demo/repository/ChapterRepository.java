package com.example.demo.repository;
import com.example.demo.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByStoryIdOrderByChapterNumberAsc(Long storyId);
    Optional<Chapter> findByStoryIdAndChapterNumber(Long storyId, Double chapterNumber);
    @Modifying
    @Query("DELETE FROM Chapter c WHERE c.story.id = :storyId")
    void deleteByStoryId(@Param("storyId") Long storyId);
}
