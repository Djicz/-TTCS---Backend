package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "chapters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "story")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;
    @Column(nullable = false)
    private Double chapterNumber;
    @Column(nullable = false)
    private String title;
    @Enumerated(EnumType.STRING)
    private ChapterType type = ChapterType.TEXT;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content; 
    @Column(columnDefinition = "JSON")
    private String images; 
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    public enum ChapterType {
        TEXT, COMIC
    }
}
