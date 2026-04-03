package com.example.demo.controller;
import com.example.demo.entity.Story;
import com.example.demo.entity.Chapter;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.service.ChapterService;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class StoryController {
    private final StoryService storyService;
    private final ChapterService chapterService;
    private final ChapterRepository chapterRepository;
    
    @GetMapping("/stories")
    public String listStories(
            @RequestParam(required = false) String keyword,
            Model model, 
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
        model.addAttribute("stories", stories.getContent());
        model.addAttribute("page", stories);
        model.addAttribute("keyword", keyword);
        return "story/list";
    }
    
    @GetMapping("/genre/{slug}")
    public String listByGenre(
            @PathVariable String slug, 
            Model model, 
            @PageableDefault(size = 16, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
    
        Page<Story> stories = storyService.getStoriesByGenre(slug, pageable);
    
        model.addAttribute("stories", stories.getContent());
        model.addAttribute("page", stories);
        model.addAttribute("genreSlug", slug);
        return "story/list";
    }

    @GetMapping("/api/test-stories-count")
    @org.springframework.web.bind.annotation.ResponseBody
    public String getStoriesCount() {
        return "Total stories: " + storyService.getStories(null, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }
    @GetMapping("/story/{slug}")
    public String viewStory(
            @PathVariable String slug, 
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Story story = storyService.getStoryBySlug(slug);
        if (story == null) {
            return "redirect:/error";
        }
        Pageable chapterPageable = PageRequest.of(page, 100, Sort.by("chapterNumber").ascending());
        Page<Chapter> chapterPage = chapterRepository.findByStoryId(story.getId(), chapterPageable);
        
        model.addAttribute("story", story);
        model.addAttribute("chapters", chapterPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", chapterPage.getTotalPages());
        model.addAttribute("hasNext", chapterPage.hasNext());
        model.addAttribute("hasPrevious", chapterPage.hasPrevious());
        model.addAttribute("relatedStories", storyService.getRelatedStories(slug, 6));
        return "story/detail";
    }
}
