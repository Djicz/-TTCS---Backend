package com.example.demo.controller;

import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;

    @PostMapping("/reading-time") //cap nhat thoi gian doc
    public ResponseEntity<Void> updateReadingTime(@RequestParam int seconds, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        userService.updateReadingTime(authentication.getName(), seconds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/claim-nomination-ticket") //cap nhat phieu de cu
    public ResponseEntity<Long> claimNominationTicket(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        Long tickets = userService.claimNominationTicket(authentication.getName());
        if (tickets != null) {
            return ResponseEntity.ok(tickets);
        }
        return ResponseEntity.notFound().build();
    }
}
