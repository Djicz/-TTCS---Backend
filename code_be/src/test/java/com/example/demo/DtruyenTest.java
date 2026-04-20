package com.example.demo;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.file.Files;
import java.nio.file.Paths;
@SpringBootTest
class DtruyenTest {
    @Test
    void testConnection() {
        try {
            String url = "https://dtruyen.com/vo-luyen-dinh-phong/";
            System.out.println("Connecting to: " + url);
            Connection.Response res = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "vi-VN,vi;q=0.9,fr-FR;q=0.8,fr;q=0.7,en-US;q=0.6,en;q=0.5")
                    .followRedirects(true)
                    .timeout(15000)
                    .execute();
            Document doc = res.parse();
            Files.writeString(Paths.get("dtruyen_story.html"), doc.html());
            System.out.println("Saved HTML to dtruyen_story.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
