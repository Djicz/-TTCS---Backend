package com.example.demo.repository;
import com.example.demo.entity.Bookshelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, Long> {
    Optional<Bookshelf> findByUserIdAndStoryId(Long userId, Long storyId);
    List<Bookshelf> findByUserIdOrderByAddedAtDesc(Long userId);
    List<Bookshelf> findByStoryIdAndNotifyOnNewChapterTrue(Long storyId);
    @Modifying
    @Query("DELETE FROM Bookshelf b WHERE b.story.id = :storyId")
    void deleteByStoryId(@Param("storyId") Long storyId);
}
