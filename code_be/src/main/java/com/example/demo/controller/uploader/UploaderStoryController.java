package com.example.demo.controller.uploader;

import com.example.demo.entity.Chapter;
import com.example.demo.entity.Role;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChapterService;
import com.example.demo.service.GenreService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.StoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/uploader")
@RequiredArgsConstructor
public class UploaderStoryController {
    private final StoryService storyService;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ChapterService chapterService;
    private final ChapterRepository chapterRepository;
    private final GenreService genreService;
    private final NotificationService notificationService;

    //lay user dang dang nhap

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    //lay story va kiem tra quyen
    private Story getOwnedStory(Long storyId, User user) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found or permission denied"));
        //kiem tra quyen
        if (story.getUploader() == null || !story.getUploader().getId().equals(user.getId())) {
            //neu uploader null hoac user khong phai uploader thi ktra admin
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new RuntimeException("Story not found or permission denied");
            } //neu cung khong phai admin thi nem exception
        }
        return story;
    }

    //lay danh sach story cua uploader
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = getCurrentUser(authentication); //lay user dang dang nhap
        List<Story> stories = storyRepository.findByUploaderId(user.getId());
        model.addAttribute("stories", stories);
        return "uploader/dashboard";
    }

    //story CRUD
    //tao form
    @GetMapping("/stories/new")
    public String showCreateForm(Model model) {
        model.addAttribute("story", new Story());
        model.addAttribute("allGenres", genreService.getAllGenres());
        return "uploader/story_form";
    }
    //create story
    @PostMapping("/stories/create")
    public String createStory(@ModelAttribute Story story,
                              @RequestParam(required = false) List<Long> genreIds,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request, HttpServletResponse response) {
        User user = getCurrentUser(authentication); //lay user
        story.setUploader(user); //gan uploader
        Story savedStory = storyService.createStory(story);
        genreService.updateStoryGenres(savedStory, genreIds); //gan the loai
        //kiem tra roll uploader
        boolean hasUploaderRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_UPLOADER"));
        if (!hasUploaderRole) { //cap roll neu chua co
            Role uploaderRole = roleRepository.findByName("ROLE_UPLOADER").orElse(null);
            if (uploaderRole != null) {
                user.getRoles().add(uploaderRole);
                userRepository.save(user);
                //quyen duoc cap nhat ngya trong session
                List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authentication.getAuthorities());
                updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_UPLOADER"));
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        authentication.getPrincipal(), authentication.getCredentials(), updatedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                new HttpSessionSecurityContextRepository()
                        .saveContext(SecurityContextHolder.getContext(), request, response);
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đăng truyện thành công!");
        return "redirect:/uploader/dashboard";
    }
    //form sua truyen
    @GetMapping("/stories/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Story story = getOwnedStory(id, user);
        model.addAttribute("story", story);
        model.addAttribute("allGenres", genreService.getAllGenres());
        return "uploader/story_form";
    }
    //sua
    @PostMapping("/stories/{id}/update")
    public String updateStory(@PathVariable Long id,
                              @ModelAttribute Story storyDetails,
                              @RequestParam(required = false) List<Long> genreIds,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);
        Story story = getOwnedStory(id, user);

        story.setTitle(storyDetails.getTitle());
        story.setSlug(storyDetails.getSlug());
        story.setAuthor(storyDetails.getAuthor());
        story.setDescription(storyDetails.getDescription());
        story.setCoverImage(storyDetails.getCoverImage());
        story.setStatus(storyDetails.getStatus());

        Story savedStory = storyService.updateStory(story);
        //cap nhat genres
        genreService.updateStoryGenres(savedStory, genreIds);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật truyện thành công!");
        return "redirect:/uploader/dashboard";
    }

    //xoa
    @PostMapping("/stories/{id}/delete")
    public String deleteStory(@PathVariable Long id, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);
        getOwnedStory(id, user); //kiem tra quyen
        storyService.deleteStory(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa truyện thành công!");
        return "redirect:/uploader/dashboard";
    }

    //chapter CRUD

    //lay danh sach chapter
    @GetMapping("/stories/{storyId}/chapters")
    public String listChapters(@PathVariable Long storyId, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Story story = getOwnedStory(storyId, user);
        List<Chapter> chapters = chapterService.getChaptersByStoryId(storyId);
        model.addAttribute("story", story);
        model.addAttribute("chapters", chapters);
        return "uploader/chapters";
    }

    //form them chuong moi
    @GetMapping("/stories/{storyId}/chapters/new")
    public String showChapterCreateForm(@PathVariable Long storyId, Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Story story = getOwnedStory(storyId, user);
        model.addAttribute("story", story);
        model.addAttribute("chapter", new Chapter());
        return "uploader/chapter_form";
    }

    //luu chuong moi
    @PostMapping("/stories/{storyId}/chapters/create")
    public String createChapter(@PathVariable Long storyId, @ModelAttribute Chapter chapter,
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);
        Story story = getOwnedStory(storyId, user);
        chapter.setStory(story);
        Chapter savedChapter = chapterService.saveChapter(chapter);
        //gui thong bao den user da luu truyen
        notificationService.notifyNewChapter(savedChapter);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm chương thành công!");
        return "redirect:/uploader/stories/" + storyId + "/chapters";
    }

    //form sua chapter
    @GetMapping("/stories/{storyId}/chapters/{chapterId}/edit")
    public String showChapterEditForm(@PathVariable Long storyId, @PathVariable Long chapterId,
                                     Model model, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Story story = getOwnedStory(storyId, user);
        Chapter chapter = chapterRepository.findByIdAndStoryId(chapterId, storyId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        model.addAttribute("story", story);
        model.addAttribute("chapter", chapter);
        return "uploader/chapter_form";
    }

    //luu chinh sua chuong
    @PostMapping("/stories/{storyId}/chapters/{chapterId}/update")
    public String updateChapter(@PathVariable Long storyId, @PathVariable Long chapterId,
                                @ModelAttribute Chapter chapterDetails,
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);
        getOwnedStory(storyId, user);
        Chapter chapter = chapterRepository.findByIdAndStoryId(chapterId, storyId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        chapter.setChapterNumber(chapterDetails.getChapterNumber());
        chapter.setTitle(chapterDetails.getTitle());
        chapter.setContent(chapterDetails.getContent());
        chapter.setType(chapterDetails.getType());

        chapterService.saveChapter(chapter);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chương thành công!");
        return "redirect:/uploader/stories/" + storyId + "/chapters";
    }

    //xoa chuong
    @PostMapping("/stories/{storyId}/chapters/{chapterId}/delete")
    public String deleteChapter(@PathVariable Long storyId, @PathVariable Long chapterId,
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);
        getOwnedStory(storyId, user);
        chapterRepository.findByIdAndStoryId(chapterId, storyId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        chapterService.deleteChapter(chapterId);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa chương thành công!");
        return "redirect:/uploader/stories/" + storyId + "/chapters";
    }
}
