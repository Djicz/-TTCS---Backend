package com.example.demo.repository;
import com.example.demo.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByStoryIdOrderByChapterNumberAsc(Long storyId);
    Page<Chapter> findByStoryIdOrderByChapterNumberAsc(Long storyId, Pageable pageable);
    Optional<Chapter> findByStoryIdAndChapterNumber(Long storyId, Double chapterNumber);
    @Modifying
    @Query("DELETE FROM Chapter c WHERE c.story.id = :storyId")
    void deleteByStoryId(@Param("storyId") Long storyId);

    Optional<Chapter> findByIdAndStoryId(Long chapterId, Long storyId);
    
    Optional<Chapter> findFirstByStoryIdAndChapterNumberGreaterThanOrderByChapterNumberAsc(Long storyId, Double chapterNumber);
    Optional<Chapter> findFirstByStoryIdAndChapterNumberLessThanOrderByChapterNumberDesc(Long storyId, Double chapterNumber);
}
