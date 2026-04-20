package com.example.demo.config;

import com.example.demo.entity.Genre;
import com.example.demo.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seed các thể loại mặc định khi khởi động nếu chưa có dữ liệu.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final GenreRepository genreRepository;

    private static final List<String[]> DEFAULT_GENRES = List.of(
            new String[]{"Ngôn Tình",            "ngon-tinh"},
            new String[]{"Tiên Hiệp",             "tien-hiep"},
            new String[]{"Kiếm Hiệp",             "kiem-hiep"},
            new String[]{"Huyền Huyễn",           "huyen-huyen"},
            new String[]{"Đô Thị",                "do-thi"},
            new String[]{"Hệ Thống",              "he-thong"},
            new String[]{"Kinh Dị",               "kinh-di"},
            new String[]{"Trinh Thám",            "trinh-tham"},
            new String[]{"Hài Hước",              "hai-huoc"},
            new String[]{"Lịch Sử",               "lich-su"},
            new String[]{"Đam Mỹ",               "dam-my"},
            new String[]{"Khoa Học Viễn Tưởng",  "khoa-hoc-vien-tuong"}
    );

    @Override
    public void run(ApplicationArguments args) {
        if (genreRepository.count() == 0) {
            for (String[] g : DEFAULT_GENRES) {
                genreRepository.save(Genre.builder()
                        .name(g[0])
                        .slug(g[1])
                        .build());
            }
            System.out.println("[DataInitializer] Đã seed " + DEFAULT_GENRES.size() + " thể loại mặc định.");
        }
    }
}
