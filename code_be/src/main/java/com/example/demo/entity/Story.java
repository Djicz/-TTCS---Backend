package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "stories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "genres", "uploader" })
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(unique = true, nullable = false)
    private String slug;
    private String author;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StoryStatus status = StoryStatus.ONGOING;
    @Builder.Default
    private Long views = 0L;
    @Builder.Default
    private Double averageRating = 0.0;
    @Builder.Default
    private Integer ratingCount = 0;
    @Builder.Default
    private Long nominations = 0L;
    private String coverImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "story_genres", joinColumns = @JoinColumn(name = "story_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public enum StoryStatus {
        ONGOING, COMPLETED, PAUSED
    }
}
