package com.example.demo.controller;

import com.example.demo.entity.Bookshelf;
import com.example.demo.entity.ReadingProgress;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.BookshelfRepository;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookshelfController.class)
@AutoConfigureMockMvc(addFilters = true)
public class BookshelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookshelfRepository bookshelfRepository;
    @MockBean
    private ReadingProgressRepository progressRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private StoryRepository storyRepository;

    private User mockUser;
    private Story mockStory;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).username("testuser").build();
        mockStory = Story.builder().id(10L).title("Truyện Test").slug("truyen-test").build();
    }

    // --- TEST SHOW BOOKSHELF ---
    @Test
    @WithMockUser(username = "testuser")
    public void testShowBookshelf_Success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(bookshelfRepository.findByUserIdOrderByAddedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(progressRepository.findByUserIdOrderByLastReadAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/user/bookshelf"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/bookshelf"))
                .andExpect(model().attributeExists("bookshelf"))
                .andExpect(model().attributeExists("historyPage"))
                .andExpect(model().attributeExists("progressList"));
    }

    // --- TEST DELETE HISTORY ---
    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteHistory_Success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/bookshelf/history/delete")
                        .param("storyId", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/bookshelf"));

        verify(progressRepository, times(1)).deleteByUserIdAndStoryId(1L, 10L);
    }

    // --- TEST ADD TO BOOKSHELF ---
    @Test
    @WithMockUser(username = "testuser")
    public void testAddToBookshelf_NewStory_ShouldSave() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(storyRepository.findById(10L)).thenReturn(Optional.of(mockStory));
        when(bookshelfRepository.findByUserIdAndStoryId(1L, 10L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/bookshelf/add")
                        .param("storyId", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/story/truyen-test"));

        verify(bookshelfRepository, times(1)).save(any(Bookshelf.class));
    }

    // --- TEST BẢO MẬT (ĐÃ SỬA LỖI) ---
    @Test
    public void testShowBookshelf_Unauthenticated_ShouldReturn401() throws Exception {
        // Sửa từ is3xxRedirection sang isUnauthorized (401) để khớp với thực tế hệ thống
        mockMvc.perform(get("/user/bookshelf"))
                .andExpect(status().isUnauthorized()); 
    }
}