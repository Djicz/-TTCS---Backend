package com.example.demo.entity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Set;

public class GenreTest {

    @Test
    public void testGenreBuilderInitialization() {
        // 1. Khởi tạo Genre bằng Builder (không truyền stories)
        Genre genre = Genre.builder()
                .name("Hành Động")
                .slug("hanh-dong")
                .build();

        // 2. Kiểm tra xem stories có bị null không
        // Nếu thiếu @Builder.Default, dòng này sẽ FAIL vì stories là null
        assertNotNull(genre.getStories(), "Lỗi: Tập hợp stories bị null! Hãy thêm @Builder.Default vào Genre.java");
        
        // 3. Kiểm tra tính năng thêm phần tử vào Set
        Story mockStory = Story.builder().title("Truyện siêu hay").build();
        genre.getStories().add(mockStory);
        
        // 4. Xác nhận số lượng phần tử
        assertEquals(1, genre.getStories().size(), "Lỗi: Không thêm được story vào Genre!");
        assertEquals("Hành Động", genre.getName());
    }

    @Test
    public void testGenreNoArgsConstructor() {
        // Kiểm tra xem Constructor mặc định (dùng cho JPA) có hoạt động không
        Genre genre = new Genre();
        assertNotNull(genre.getStories(), "Lỗi: NoArgsConstructor không khởi tạo được HashSet!");
    }
}