package com.example.demo.repository;

import com.example.demo.entity.Bookshelf;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Chỉ load các cấu hình liên quan đến JPA và Database tạm (H2)
@ActiveProfiles("test")
public class BookshelfRepositoryTest {

    @Autowired
    private BookshelfRepository bookshelfRepository;

    @Autowired
    private TestEntityManager entityManager; // Dùng để tạo dữ liệu mẫu nhanh

    @Test
    public void testFindByUserIdAndStoryId() {
        // 1. Tạo dữ liệu giả và lưu vào DB tạm
        User user = User.builder().username("testuser").email("test@example.com").password("123456").build();
        Story story = Story.builder().title("Truyện Hay").slug("truyen-hay").build();
        entityManager.persist(user);
        entityManager.persist(story);

        Bookshelf shelf = Bookshelf.builder().user(user).story(story).build();
        bookshelfRepository.save(shelf);

        // 2. Chạy hàm cần test
        var result = bookshelfRepository.findByUserIdAndStoryId(user.getId(), story.getId());

        // 3. Kiểm tra kết quả
        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getUser().getId());
    }
}