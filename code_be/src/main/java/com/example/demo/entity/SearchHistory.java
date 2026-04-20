package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "search_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String keyword;
    @Column(nullable = false)
    private Long searchCount = 1L;
    private LocalDateTime lastSearchedAt;
    @PrePersist
    @PreUpdate
    protected void onSave() {
        lastSearchedAt = LocalDateTime.now();
    }
}
