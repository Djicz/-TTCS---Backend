package com.example.demo.controller;

import com.example.demo.entity.Genre;
import com.example.demo.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/genres")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public String listGenres(Model model) {
        List<Genre> genres = genreService.getAllGenres();
        model.addAttribute("genres", genres);
        return "admin/genre-list";
    }

    @PostMapping("/add")
    public String addGenre(@RequestParam String name, @RequestParam String slug, RedirectAttributes ra) {
        try {
            genreService.createGenre(name, slug);
            ra.addFlashAttribute("success", "Thêm thể loại thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: Slug có thể đã tồn tại.");
        }
        return "redirect:/admin/genres";
    }

    @PostMapping("/edit")
        public String editGenre(@RequestParam Long id, @RequestParam String name, @RequestParam String slug, RedirectAttributes ra) {
            try {
                genreService.updateGenre(id, name, slug);
                ra.addFlashAttribute("success", "Cập nhật thành công!");
            } catch (Exception e) {
                ra.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
            }
                return "redirect:/admin/genres";
        }
    
    @PostMapping("/delete/{id}")
    public String deleteGenre(@PathVariable Long id, RedirectAttributes ra) {
        try {
            genreService.deleteGenre(id);
            ra.addFlashAttribute("success", "Xóa thể loại thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa thể loại đang có truyện.");
        }
        return "redirect:/admin/genres";
    }
}