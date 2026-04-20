package com.example.demo.controller;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("username", authentication.getName());
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
            boolean isUploader = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_UPLOADER"));
            model.addAttribute("isUploader", isUploader);
            boolean isMod = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MOD"));
            model.addAttribute("isMod", isMod);
            // Đếm thông báo chưa đọc cho badge trên navbar
            userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
                long unread = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
                model.addAttribute("unreadNotifCount", unread);
            });
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model,
                                         HttpServletResponse response) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi.";
        if (msg.contains("permission") || msg.contains("not found")) {
            response.setStatus(403);
            model.addAttribute("errorTitle", "Không có quyền truy cập");
            model.addAttribute("errorMessage", "Truyện không tồn tại hoặc bạn không có quyền thao tác.");
        } else {
            response.setStatus(500);
            model.addAttribute("errorTitle", "Lỗi hệ thống");
            model.addAttribute("errorMessage", msg);
        }
        return "error/generic";
    }
}
