package com.example.demo.controller;
import com.example.demo.entity.Story;
import com.example.demo.service.ChapterService;
import com.example.demo.service.GenreService;
import com.example.demo.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final GenreService genreService;
    private final com.example.demo.repository.CommentRepository commentRepository;

    @GetMapping("/stories")
    public String listStories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genre,
            Model model,
            @PageableDefault(size = 16, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Specification<Story> spec = null;

        // Filter theo từ khoá (title hoặc author)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likePattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")),  likePattern),
                cb.like(cb.lower(root.get("author")), likePattern)
            );
        }

        // Filter theo thể loại (genre slug)
        if (genre != null && !genre.trim().isEmpty()) {
            final String genreSlug = genre.trim();
            Specification<Story> genreSpec = (root, query, cb) -> {
                query.distinct(true);
                var genreJoin = root.join("genres");
                return cb.equal(genreJoin.get("slug"), genreSlug);
            };
            spec = (spec == null) ? genreSpec : spec.and(genreSpec);
        }

        Page<Story> stories = storyService.getStories(spec, pageable);
        model.addAttribute("stories",      stories.getContent());
        model.addAttribute("page",         stories);
        model.addAttribute("keyword",      keyword);
        model.addAttribute("genre",        genre);
        model.addAttribute("allGenres",    genreService.getAllGenres());
        return "story/list";
    }

    @GetMapping("/api/test-stories-count")
    @org.springframework.web.bind.annotation.ResponseBody
    public String getStoriesCount() {
        return "Total stories: " + storyService.getStories(null, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    @GetMapping("/story/{slug}")
    public String viewStory(@PathVariable String slug, 
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Story story = storyService.getStoryBySlug(slug);
        model.addAttribute("story", story);
        
        // Paginated chapters (50 per page)
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 50);
        Page<com.example.demo.entity.Chapter> chapterPage = chapterService.getChaptersByStoryId(story.getId(), pageable);
        
        model.addAttribute("chapters",     chapterPage.getContent());
        model.addAttribute("chapterPage",  chapterPage);
        model.addAttribute("firstChapterId", chapterService.getFirstChapterId(story.getId()));
        
        model.addAttribute("comments", commentRepository.findByStoryIdAndParentCommentIsNullOrderByCreatedAtDesc(story.getId()));
        return "story/detail";
    }
}
