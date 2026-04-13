package com.example.demo.controller.web.api;

import com.example.demo.entity.ReportType;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody ReportRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập để báo cáo."));
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập để báo cáo."));
        }

        try {
            ReportType type = ReportType.valueOf(request.getType().toUpperCase());
            String resultMsg = reportService.sendReport(
                    userOpt.get(),
                    type,
                    request.getTargetId(),
                    request.getReason(),
                    request.getContent()
            );
            return ResponseEntity.ok(Map.of("message", resultMsg));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Loại báo cáo không hợp lệ."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Đã xảy ra lỗi khi gửi báo cáo: " + e.getMessage()));
        }
    }

    public static class ReportRequest {
        private String type;
        private Long targetId;
        private String reason;
        private String content;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
