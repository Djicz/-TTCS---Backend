package com.example.demo.repository;

import com.example.demo.entity.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Chỉ load cấu hình liên quan đến JPA để test đơn lẻ
@ActiveProfiles("test") // Sử dụng cấu hình H2 để không đụng vào MySQL thật
public class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private TestEntityManager entityManager; // Dùng để lưu dữ liệu mẫu vào DB ảo

    @Test
    public void testFindBySlug_Success() {
        // 1. Tạo dữ liệu mẫu
        Genre genre = Genre.builder()
                .name("Tiên Hiệp")
                .slug("tien-hiep")
                .build();
        entityManager.persist(genre); // Lưu vào database tạm
        entityManager.flush();

        // 2. Chạy hàm cần test
        Optional<Genre> found = genreRepository.findBySlug("tien-hiep");

        // 3. Kiểm tra kết quả
        assertTrue(found.isPresent());
        assertEquals("Tiên Hiệp", found.get().getName());
    }

    @Test
    public void testFindBySlug_NotFound() {
        // Chạy hàm với slug không tồn tại
        Optional<Genre> found = genreRepository.findBySlug("ngon-tinh-khong-ton-tai");

        // Kiểm tra kết quả phải là Empty
        assertFalse(found.isPresent());
    }
}