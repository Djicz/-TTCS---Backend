package com.example.demo.controller.app;
import com.example.demo.entity.Story;
import com.example.demo.service.ChapterService;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/app")
public class AppStoryController {
    private final StoryService storyService;
    private final ChapterService chapterService;
    @GetMapping("/stories")
    public ResponseEntity<?> listStories(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 16, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Specification<Story> spec = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likePattern = "%" + keyword.trim() + "%";
            spec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern.toLowerCase()),
                    cb.like(cb.lower(root.get("author")), likePattern.toLowerCase())
            );
        }

        Page<Story> stories = storyService.getStories(spec, pageable);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("stories", stories.getContent());
        response.put("page", stories);
        response.put("keyword", keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/test-stories-count")
    @org.springframework.web.bind.annotation.ResponseBody
    public String getStoriesCount() {
        return "Total stories: " + storyService.getStories(null, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }
    @GetMapping("/story/{slug}")
    public ResponseEntity<?> viewStory(@PathVariable String slug) {
        Story story = storyService.getStoryBySlug(slug);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("story", story);
        response.put("chapters", chapterService.getChaptersByStoryId(story.getId()));
        return ResponseEntity.ok(response);
    }
}