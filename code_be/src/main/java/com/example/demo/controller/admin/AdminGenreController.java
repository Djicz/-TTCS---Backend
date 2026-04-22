package com.example.demo.controller.admin;

import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.service.GenreService;
import com.example.demo.service.StoryService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/genres")
@RequiredArgsConstructor
public class AdminGenreController {

    private final GenreService genreService;
    private final StoryService storyService;
    private final UserService userService;

    // Quyen

    private boolean isAdmin(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isAdminOrMod(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_MOD"));
    }

    private User getCurrentUser(Authentication auth) {
        User user = userService.getUserByUsername(auth.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    //Quan ly the loai

    //Danh sach the loai + form them moi
    @GetMapping
    public String listGenres(Model model, Authentication auth) {
        if (!isAdminOrMod(auth)) return "redirect:/";
        model.addAttribute("genres", genreService.getAllGenres());
        return "admin/genres";
    }

    //Tao the loai moi
    @PostMapping("/create")
    public String createGenre(@RequestParam String name,
                              @RequestParam(required = false) String slug,
                              Authentication auth,
                              RedirectAttributes ra) {
        if (!isAdmin(auth)) return "redirect:/";
        if (name == null || name.trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Tên thể loại không được để trống!");
            return "redirect:/admin/genres";
        }
        String genreSlug = (slug != null && !slug.trim().isEmpty())
                ? slug.trim().toLowerCase()
                : toSlug(name);
        try {
            genreService.createGenre(name.trim(), genreSlug);
            ra.addFlashAttribute("successMessage", "Tạo thể loại \"" + name.trim() + "\" thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Slug đã tồn tại hoặc có lỗi: " + e.getMessage());
        }
        return "redirect:/admin/genres";
    }

    //xoa genres
    @PostMapping("/{id}/delete")
    public String deleteGenre(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        if (!isAdmin(auth)) return "redirect:/";
        try {
            genreService.deleteGenre(id);
            ra.addFlashAttribute("successMessage", "Đã xoá thể loại.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xoá: " + e.getMessage());
        }
        return "redirect:/admin/genres";
    }

    //Gan the loai (admin/mod)

    //form gan genre cho 1 truyen
    @GetMapping("/story/{storyId}")
    public String showAssignForm(@PathVariable Long storyId, Model model, Authentication auth) {
        if (!isAdminOrMod(auth)) return "redirect:/";
        Story story = storyService.getStoryById(storyId);
        model.addAttribute("story",     story);
        model.addAttribute("allGenres", genreService.getAllGenres());
        return "admin/story_genres";
    }

    //Luu genre duoc gan
    @PostMapping("/story/{storyId}")
    public String assignGenres(@PathVariable Long storyId,
                               @RequestParam(required = false) List<Long> genreIds,
                               Authentication auth,
                               RedirectAttributes ra) {
        if (!isAdminOrMod(auth)) return "redirect:/";
        Story story = storyService.getStoryById(storyId);
        genreService.updateStoryGenres(story, genreIds);
        ra.addFlashAttribute("successMessage", "Đã cập nhật thể loại cho \"" + story.getTitle() + "\"!");
        return "redirect:/story/" + story.getSlug();
    }

    //Chuan hoa slug

    private String toSlug(String name) {
        return name.trim().toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim().replaceAll("\\s+", "-");
    }
}
