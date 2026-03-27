package com.example.demo.repository;

import com.example.demo.entity.Genre;
import com.example.demo.entity.Story;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class StoryRepositoryTest {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testFindByGenres_Slug_Success() {
        // 1. Tạo các thể loại mẫu
        Genre action = Genre.builder().name("Hành Động").slug("hanh-dong").build();
        Genre magic = Genre.builder().name("Ma Pháp").slug("ma-phap").build();
        entityManager.persist(action);
        entityManager.persist(magic);

        // 2. Tạo truyện và gắn thẻ tag "Hành Động"
        Story story1 = Story.builder()
                .title("Võ Thần Chúa Tể")
                .slug("vo-than-chua-te")
                .genres(Set.of(action))
                .build();
        
        // Tạo truyện và gắn thẻ tag "Ma Pháp"
        Story story2 = Story.builder()
                .title("Pháp Sư Vô Diện")
                .slug("phap-su-vo-dien")
                .genres(Set.of(magic))
                .build();

        entityManager.persist(story1);
        entityManager.persist(story2);
        entityManager.flush();

        // 3. Thực hiện kiểm tra lọc theo tag "hanh-dong"
        Page<Story> actionStories = storyRepository.findByGenres_Slug("hanh-dong", PageRequest.of(0, 10));

        // 4. Kiểm chứng kết quả
        assertEquals(1, actionStories.getTotalElements());
        assertEquals("Võ Thần Chúa Tể", actionStories.getContent().get(0).getTitle());
        
        // Kiểm tra truyện "Pháp Sư Vô Diện" không nằm trong kết quả này
        assertTrue(actionStories.getContent().stream()
                .noneMatch(s -> s.getTitle().equals("Pháp Sư Vô Diện")));
    }

    @Test
    public void testFindByGenres_Slug_NoResult() {
        // Truy vấn một tag không tồn tại hoặc không có truyện nào gắn
        Page<Story> results = storyRepository.findByGenres_Slug("kinh-di", PageRequest.of(0, 10));

        assertTrue(results.isEmpty());
    }
}