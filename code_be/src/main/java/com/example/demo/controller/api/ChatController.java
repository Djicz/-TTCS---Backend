package com.example.demo.controller.api;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    @PostMapping("/add")
    public ResponseEntity<?> addMessage(
            @RequestParam String content,
            @RequestParam(required = false) Long recipientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(403).body("User not found");
        }
        User recipient = null;
        if (recipientId != null) {
            recipient = userRepository.findById(recipientId).orElse(null);
        }
        ChatMessage message = ChatMessage.builder()
                .user(user)
                .recipient(recipient)
                .content(content.trim())
                .createdAt(LocalDateTime.now())
                .build();
        chatMessageRepository.save(message);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/private")
    public ResponseEntity<?> getPrivateMessages(
            @RequestParam Long recipientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(403).body("User not found");
        }
        List<ChatMessage> messages = chatMessageRepository.findPrivateMessages(user.getId(), recipientId);
        var dtoList = messages.stream().map(m -> java.util.Map.of(
                "id", m.getId(),
                "senderId", m.getUser().getId(),
                "senderName", m.getUser().getUsername(),
                "content", m.getContent(),
                "createdAt", m.getCreatedAt())).toList();
        return ResponseEntity.ok(dtoList);
    }
}
