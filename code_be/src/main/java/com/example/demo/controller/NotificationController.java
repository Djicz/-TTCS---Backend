package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    //lay notification tu user
    @GetMapping("/notifications")
    public String notificationsPage(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        List<Notification> notifications = notificationService.getNotifications(user.getId());
        model.addAttribute("notifications", notifications);
        return "user/notifications";
    }

    //danh dau noti da doc
    @PostMapping("/api/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<?> markRead(@PathVariable Long id, Authentication auth) {
        User user = getCurrentUser(auth);
        notificationService.markRead(id, user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    //danh dau tat ca noti la da doc
    @PostMapping("/api/notifications/read-all")
    @ResponseBody
    public ResponseEntity<?> markAllRead(Authentication auth) {
        User user = getCurrentUser(auth);
        notificationService.markAllRead(user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // lay so noti chua doc
    @GetMapping("/api/notifications/unread-count")
    @ResponseBody
    public ResponseEntity<?> unreadCount(Authentication auth) {
        User user = getCurrentUser(auth);
        long count = notificationService.countUnread(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
}
