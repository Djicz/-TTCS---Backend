package com.example.demo.controller.api;

import com.example.demo.entity.ChatMessage;
import com.example.demo.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;

    @PostMapping("/add")
    public ResponseEntity<?> addMessage(
            @RequestParam String content,
            @RequestParam(required = false) Long recipientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }
        
        try {
            chatMessageService.addMessage(userDetails.getUsername(), content, recipientId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/private")
    public ResponseEntity<?> getPrivateMessages(
            @RequestParam Long recipientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        try {
            List<ChatMessage> messages = chatMessageService.getPrivateMessages(userDetails.getUsername(), recipientId);
            var dtoList = messages.stream().map(m -> java.util.Map.of(
                    "id", m.getId(),
                    "senderId", m.getUser().getId(),
                    "senderName", m.getUser().getUsername(),
                    "content", m.getContent(),
                    "createdAt", m.getCreatedAt())).toList();
            return ResponseEntity.ok(dtoList);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
