package com.example.demo.service;

import com.example.demo.entity.Report;
import com.example.demo.entity.ReportType;
import com.example.demo.entity.User;
import com.example.demo.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    @Transactional
    public String sendReport(User reporter, ReportType type, Long targetId, String reason, String content) {
        // 1. Đếm xem người dùng này đã báo cáo mục này bao nhiêu lần rồi
        long userReportCount = reportRepository.countByReporterIdAndTypeAndTargetId(
                reporter.getId(), type, targetId);
        
        // 2. Giới hạn tối đa 20 lần cho mỗi người dùng trên cùng 1 nội dung
        if (userReportCount >= 20) {
            return "Bạn đã đạt giới hạn 20 lần báo cáo cho nội dung này. Cảm ơn sự nhiệt tình của bạn!";
        }

        // 3. Lưu báo cáo mới
        Report report = Report.builder()
                .reporter(reporter)
                .type(type)
                .targetId(targetId)
                .reason(reason)
                .content(content)
                .build();

        reportRepository.save(report);
        return "Gửi báo cáo thành công (Lần " + (userReportCount + 1) + "/20). Admin sẽ xem xét sớm nhất.";
    }
}