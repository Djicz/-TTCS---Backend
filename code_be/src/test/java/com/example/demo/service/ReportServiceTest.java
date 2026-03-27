package com.example.demo.service;

import com.example.demo.entity.Report;
import com.example.demo.entity.ReportType;
import com.example.demo.entity.User;
import com.example.demo.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = User.builder().id(1L).username("reporter_test").build();
    }

    @Test
    void testSendReport_Story_Success() {
        // Giả lập chưa có báo cáo nào
        when(reportRepository.countByReporterIdAndTypeAndTargetId(1L, ReportType.STORY, 10L)).thenReturn(0L);

        String result = reportService.sendReport(mockUser, ReportType.STORY, 10L, "Nội dung nhạy cảm", "Chi tiết...");

        // Kiểm tra kết quả và xác nhận đã lưu
        assertEquals("Gửi báo cáo thành công (Lần 1/20). Admin sẽ xem xét sớm nhất.", result);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void testSendReport_Comment_Success() {
        when(reportRepository.countByReporterIdAndTypeAndTargetId(1L, ReportType.COMMENT, 100L)).thenReturn(5L);

        String result = reportService.sendReport(mockUser, ReportType.COMMENT, 100L, "Spam", "");

        // Lần báo cáo thứ 6
        assertEquals("Gửi báo cáo thành công (Lần 6/20). Admin sẽ xem xét sớm nhất.", result);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void testSendReport_ReachLimit_ShouldFail() {
        // Giả lập người dùng đã báo cáo đủ 20 lần
        when(reportRepository.countByReporterIdAndTypeAndTargetId(1L, ReportType.STORY, 10L)).thenReturn(20L);

        String result = reportService.sendReport(mockUser, ReportType.STORY, 10L, "Lý do khác", "");

        // Kiểm tra thông báo chặn spam
        assertEquals("Bạn đã đạt giới hạn 20 lần báo cáo cho nội dung này. Cảm ơn sự nhiệt tình của bạn!", result);
        verify(reportRepository, never()).save(any(Report.class));
    }
}