package com.example.demo.service;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ChatMessage addMessage(String username, String content, Long recipientId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        
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
                
        ChatMessage saved = chatMessageRepository.save(message);
        
        // Gửi thông báo cho người nhận nếu là chat riêng
        if (recipient != null) {
            notificationService.notifyNewPrivateChat(user, recipient);
        }
        
        return saved;
    }

    public List<ChatMessage> getPrivateMessages(String username, Long recipientId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return chatMessageRepository.findPrivateMessages(user.getId(), recipientId);
    }
    
    public List<ChatMessage> getRecentCommunityMessages() {
        return chatMessageRepository.findByRecipientIsNullOrderByCreatedAtDesc(PageRequest.of(0, 50));
    }
}
