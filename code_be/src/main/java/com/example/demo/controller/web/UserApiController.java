package com.example.demo.controller.web;
import com.example.demo.entity.InventoryItem;
import com.example.demo.entity.User;
import com.example.demo.repository.InventoryItemRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @PostMapping("/reading-time")
    public ResponseEntity<Void> updateReadingTime(@RequestParam int seconds, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        if (seconds <= 0) {
            return ResponseEntity.badRequest().build();
        }
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            long actualSeconds = Math.min(seconds, 60);
            long currentSeconds = user.getReadingTimeSeconds() != null ? user.getReadingTimeSeconds() : 0L;
            user.setReadingTimeSeconds(currentSeconds + actualSeconds);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Nhận 1 phiếu đề cử (NOMINATION_TICKET) khi đọc chương đủ 10 giây.
     * Frontend sẽ gọi endpoint này sau khi icon hộp quà hiện lên và user bấm vào.
     */
    @PostMapping("/claim-nomination-ticket")
    public ResponseEntity<?> claimNominationTicket(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để nhận phiếu đề cử.");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("Không tìm thấy tài khoản.");
        }
        // Tìm hoặc tạo mới item NOMINATION_TICKET trong inventory
        InventoryItem ticket = inventoryItemRepository
                .findByUserIdAndItemType(user.getId(), "NOMINATION_TICKET")
                .orElseGet(() -> InventoryItem.builder()
                        .user(user)
                        .itemType("NOMINATION_TICKET")
                        .quantity(0)
                        .value(1)
                        .description("Phiếu đề cử truyện")
                        .build());
        ticket.setQuantity(ticket.getQuantity() + 1);
        inventoryItemRepository.save(ticket);
        return ResponseEntity.ok(Map.of(
                "message", "Nhận thành công 1 phiếu đề cử!",
                "totalTickets", ticket.getQuantity()
        ));
    }
}

