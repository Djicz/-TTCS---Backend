package com.example.demo.service;

import com.example.demo.entity.Genre;
import com.example.demo.entity.Story;
import com.example.demo.repository.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private StoryService storyService;

    private Story currentStory;
    private Genre genre1, genre2, genre3;

    @BeforeEach
    void setUp() {
        // Sử dụng Builder pattern từ Lombok để khởi tạo Genre
        genre1 = Genre.builder().id(1L).name("Tiên Hiệp").slug("tien-hiep").build();
        genre2 = Genre.builder().id(2L).name("Huyền Huyễn").slug("huyen-huyen").build();
        genre3 = Genre.builder().id(3L).name("Trọng Sinh").slug("trong-sinh").build();

        // Khởi tạo bộ truyện đang xem (Target)
        currentStory = new Story();
        currentStory.setId(1L);
        currentStory.setSlug("truyen-goc");
        currentStory.setGenres(new HashSet<>(Arrays.asList(genre1, genre2, genre3)));
    }

    @Test
    void testGetRelatedStories_SortingLogic() {
        // Giả lập danh sách ứng viên (Candidates) từ Database
        
        // Truyện B: Trùng 3 genre, 10 đề cử
        Story storyB = createMockStory(2L, "Truyện B", 10L, genre1, genre2, genre3);
        
        // Truyện C: Trùng 3 genre, 50 đề cử (Phải xếp thứ 1 vì cùng số genre nhưng đề cử cao hơn B)
        Story storyC = createMockStory(3L, "Truyện C", 50L, genre1, genre2, genre3);
        
        // Truyện D: Trùng 2 genre, 100 đề cử (Xếp sau B, C vì ít genre trùng hơn)
        Story storyD = createMockStory(4L, "Truyện D", 100L, genre1, genre2);
        
        // Truyện E: Trùng 1 genre, 500 đề cử (Xếp cuối cùng)
        Story storyE = createMockStory(5L, "Truyện E", 500L, genre1);

        List<Story> mockCandidates = Arrays.asList(storyB, storyC, storyD, storyE);

        // Giả lập hành vi của Repository
        when(storyRepository.findBySlug("truyen-goc")).thenReturn(Optional.of(currentStory));
        when(storyRepository.findStoriesWithCommonGenres(anySet(), anyLong(), any(PageRequest.class)))
                .thenReturn(mockCandidates);

        // Thực thi logic nghiệp vụ tại Service
        List<Story> result = storyService.getRelatedStories("truyen-goc", 6);

        // Kiểm chứng kết quả (Assertions)
        assertEquals(4, result.size());
        assertEquals("Truyện C", result.get(0).getTitle());
        assertEquals("Truyện B", result.get(1).getTitle());
        assertEquals("Truyện D", result.get(2).getTitle());
        assertEquals("Truyện E", result.get(3).getTitle());
    }
    
    @Test
    void testGetMostNominatedStories_ShouldReturnTop10() {
        // Giả lập 2 truyện với số lượt đề cử khác nhau
        Story top1 = createMockStory(10L, "Truyện Siêu Hot", 999L);
        Story top2 = createMockStory(11L, "Truyện Hot Vừa", 500L);
        
        List<Story> mockList = Arrays.asList(top1, top2);

        // Giả lập Repository trả về danh sách theo yêu cầu limit = 10
        when(storyRepository.findTopByNominations(any(PageRequest.class)))
                .thenReturn(mockList);

        // Gọi hàm service mới
        List<Story> result = storyService.getMostNominatedStories(10);

        // Kiểm chứng
        assertEquals(2, result.size());
        assertEquals("Truyện Siêu Hot", result.get(0).getTitle());
        assertEquals(999L, result.get(0).getNominations());
    }

    // Hàm bổ trợ tạo đối tượng Story nhanh để test
    private Story createMockStory(Long id, String title, Long nominations, Genre... genres) {
        Story s = new Story();
        s.setId(id);
        s.setTitle(title);
        s.setNominations(nominations);
        s.setGenres(new HashSet<>(Arrays.asList(genres)));
        return s;
    }
}