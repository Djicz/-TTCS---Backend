package com.example.demo.controller.web;

import com.example.demo.entity.InventoryItem;
import com.example.demo.entity.User;
import com.example.demo.repository.InventoryItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryApiController {
    private final StoryService storyService;
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementView(@PathVariable Long id) {
        storyService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/nominate")
    public ResponseEntity<?> nominateStory(@PathVariable Long id, Authentication authentication) {
        // 1. Phải đăng nhập
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập để đề cử."));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Không tìm thấy tài khoản."));
        }

        // 2. Kiểm tra phiếu đề cử trong inventory
        InventoryItem ticket = inventoryItemRepository
                .findByUserIdAndItemType(user.getId(), "NOMINATION_TICKET")
                .orElse(null);

        if (ticket == null || ticket.getQuantity() <= 0) {
            return ResponseEntity.status(400)
                    .body(Map.of("error", "Bạn không có đủ phiếu đề cử! Hãy đọc truyện 10 giây để nhận thêm nhé."));
        }

        // 3. Trừ 1 phiếu
        ticket.setQuantity(ticket.getQuantity() - 1);
        inventoryItemRepository.save(ticket);

        // 4. Tăng nominations của truyện
        Long newNominations = storyService.nominateStory(id);
        return ResponseEntity.ok(newNominations);
    }
}

