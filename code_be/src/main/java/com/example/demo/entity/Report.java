package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Column(nullable = false)
    private Long targetId; // ID của Story hoặc Comment bị báo cáo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter; // Người gửi báo cáo

    @Column(nullable = false)
    private String reason; // Lý do chính (ví dụ: Nội dung thô tục, Spam, Bản quyền...)

    @Column(columnDefinition = "TEXT")
    private String content; // Nội dung chi tiết/mô tả thêm từ người báo cáo

    @Builder.Default
    private boolean processed = false; // Đã được Admin xử lý chưa

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}