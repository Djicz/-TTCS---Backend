package com.example.demo.controller;

import com.example.demo.entity.Chapter;
import com.example.demo.entity.Story;
import com.example.demo.service.ChapterService;
import com.example.demo.service.StoryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChapterController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChapterControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ChapterService chapterService;
    @MockBean
    private StoryService storyService;

    @Test
    void testReadChapter_ShouldIncrementViews() throws Exception {
        // Giả lập dữ liệu: Một chương thuộc về một bộ truyện có ID là 100
        Story mockStory = new Story();
        mockStory.setId(100L);

        Chapter mockChapter = new Chapter();
        mockChapter.setId(1L);
        mockChapter.setStory(mockStory);

        // Giả lập hành vi của Service
        when(chapterService.getChapterById(1L)).thenReturn(mockChapter);

        // Thực hiện request GET tới trang đọc chương
        mockMvc.perform(get("/reader/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("story/reader"));

        // KIỂM TRA QUAN TRỌNG: Hàm incrementViews phải được gọi đúng 1 lần với ID là 100
        verify(storyService, times(1)).incrementViews(100L);
    }
}