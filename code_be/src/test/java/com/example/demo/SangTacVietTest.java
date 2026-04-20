package com.example.demo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest
class SangTacVietTest {
    @Test
    void testScraping() {
        try {
            System.out.println("============== BEGIN SCRAPING TEST ==============");
            String url = "https://sangtacviet.app/index.php?ngmar=chapterlist&h=qidian&bookid=1&sajax=getchapterlist";
            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Accept-Language", "en-US,en;q=0.9,vi;q=0.8")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .ignoreContentType(true)
                    .timeout(10000)
                    .get();
            String json = doc.body().text();
            System.out.println("JSON Length: " + json.length());
            System.out.println("JSON Preview: " + json.substring(0, Math.min(json.length(), 1000)));
            System.out.println("============== END SCRAPING TEST ==============");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
