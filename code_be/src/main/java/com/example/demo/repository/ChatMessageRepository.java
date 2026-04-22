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
    List<ChatMessage> findByRecipientIsNullOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);
    @Query("SELECT c FROM ChatMessage c WHERE (c.user.id = :user1 AND c.recipient.id = :user2) OR (c.user.id = :user2 AND c.recipient.id = :user1) ORDER BY c.createdAt ASC")
    List<ChatMessage> findPrivateMessages(@Param("user1") Long user1, @Param("user2") Long user2);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.user.id = :userId OR c.recipient.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
