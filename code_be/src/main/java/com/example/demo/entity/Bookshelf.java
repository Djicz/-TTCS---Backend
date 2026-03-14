package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "bookshelf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookshelf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;
    private Boolean notifyOnNewChapter = true;
    private LocalDateTime addedAt;
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
