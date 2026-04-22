package com.example.demo.controller.api;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.ReadingProgressService;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressApiController {
    private final ReadingProgressService readingProgressService;

    @PostMapping("/save")
    public ResponseEntity<?> saveProgress(@RequestBody ProgressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.ok().build(); 
            
        readingProgressService.saveProgressApi(
            userDetails.getUsername(), 
            request.getStoryId(), 
            request.getChapterId(), 
            request.getScrollPercentage()
        );

        return ResponseEntity.ok().build();
    }
    
    @Data
    static class ProgressRequest {
        private Long storyId;
        private Long chapterId;
        private Double scrollPercentage;
    }
}
