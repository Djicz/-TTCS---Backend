package com.example.demo.controller;

import com.example.demo.entity.Genre;
import com.example.demo.service.GenreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GenreController.class) // Chỉ test riêng GenreController
public class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService; // Mock nghiệp vụ để chạy đơn lẻ

    @Test
    @WithMockUser(roles = "ADMIN") // Giả lập quyền Admin
    public void testAddGenre_Success() throws Exception {
        mockMvc.perform(post("/admin/genres/add")
                        .param("name", "Hành Động")
                        .param("slug", "hanh-dong")
                        .with(csrf())) // Phải có CSRF vì là request POST
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/genres"))
                .andExpect(flash().attribute("success", "Thêm thể loại thành công!"));

        verify(genreService, times(1)).createGenre(eq("Hành Động"), eq("hanh-dong"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditGenre_Success() throws Exception {
        mockMvc.perform(post("/admin/genres/edit")
                        .param("id", "1")
                        .param("name", "Kiếm Hiệp")
                        .param("slug", "kiem-hiep")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/genres"))
                .andExpect(flash().attribute("success", "Cập nhật thành công!"));

        verify(genreService, times(1)).updateGenre(eq(1L), eq("Kiếm Hiệp"), eq("kiem-hiep"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteGenre_Success() throws Exception {
        mockMvc.perform(post("/admin/genres/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/genres"))
                .andExpect(flash().attribute("success", "Xóa thể loại thành công!"));

        verify(genreService, times(1)).deleteGenre(1L);
    }
}