package com.example.demo.repository;
import com.example.demo.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT c FROM ChatMessage c WHERE c.recipient IS NULL ORDER BY c.createdAt DESC")
    List<ChatMessage> findLatestMessages(Pageable pageable);
    @Query("SELECT c FROM ChatMessage c WHERE (c.user.id = :user1 AND c.recipient.id = :user2) OR (c.user.id = :user2 AND c.recipient.id = :user1) ORDER BY c.createdAt ASC")
    List<ChatMessage> findPrivateMessages(@Param("user1") Long user1, @Param("user2") Long user2);
}
