package com.example.demo.repository;

import com.example.demo.entity.Report;
import com.example.demo.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    // Tìm kiếm các báo cáo chưa xử lý, sắp xếp mới nhất lên đầu (cho Admin)
    Page<Report> findByProcessedFalseOrderByCreatedAtDesc(Pageable pageable);

    // Kiểm tra xem một người dùng đã báo cáo mục này chưa (tránh spam báo cáo)
    boolean existsByReporterIdAndTypeAndTargetId(Long reporterId, ReportType type, Long targetId);

    // Lấy lịch sử báo cáo của một người dùng cụ thể
    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    long countByReporterIdAndTypeAndTargetId(Long reporterId, ReportType type, Long targetId);
}