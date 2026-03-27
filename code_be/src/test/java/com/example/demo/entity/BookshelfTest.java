package com.example.demo.entity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

public class BookshelfTest {

    private User mockUser;
    private Story mockStory;

    @BeforeEach
    public void setUp() {
        // Giả lập dữ liệu đầu vào cho User và Story
        mockUser = User.builder().id(1L).username("testuser").build();
        mockStory = Story.builder().id(100L).title("Truyện Test").build();
    }

    @Test
    public void testFullBookshelfLogic() {
        // 1. Test khởi tạo với đầy đủ quan hệ
        Bookshelf shelf = Bookshelf.builder()
                .user(mockUser)
                .story(mockStory)
                .notifyOnNewChapter(false) // Test thay đổi giá trị mặc định
                .build();

        // Giả lập lưu vào DB để kích hoạt @PrePersist
        shelf.onCreate();

        // Kiểm tra định danh
        assertEquals("testuser", shelf.getUser().getUsername(), "Lỗi: Sai thông tin người dùng!");
        assertEquals("Truyện Test", shelf.getStory().getTitle(), "Lỗi: Sai thông tin truyện!");
        
        // Kiểm tra logic thời gian
        assertNotNull(shelf.getAddedAt());
        assertTrue(shelf.getAddedAt().isBefore(LocalDateTime.now().plusSeconds(1)));

        // Kiểm tra logic thông báo
        assertFalse(shelf.getNotifyOnNewChapter(), "Lỗi: Không lưu được trạng thái tắt thông báo!");
    }

    @Test
    public void testNotifyDefaultValue() {
        // Test xem nếu không set thì có tự động là true không
        Bookshelf shelf = Bookshelf.builder().build();
        // Lưu ý: @Builder.Default sẽ đảm bảo giá trị này là true
        assertTrue(shelf.getNotifyOnNewChapter(), "Lỗi: Giá trị mặc định thông báo phải là True!");
    }
}