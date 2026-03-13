package com.example.demo.repository;
import com.example.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByStoryIdAndParentCommentIsNullOrderByCreatedAtDesc(Long storyId);
    List<Comment> findByChapterIdAndParentCommentIsNullOrderByCreatedAtDesc(Long chapterId);
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.parentComment IS NOT NULL AND c.story.id = :storyId")
    void deleteRepliesByStoryId(@Param("storyId") Long storyId);
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.parentComment IS NULL AND c.story.id = :storyId")
    void deleteRootCommentsByStoryId(@Param("storyId") Long storyId);
}
