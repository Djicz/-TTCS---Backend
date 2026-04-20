package com.example.demo.service;
import com.example.demo.entity.Chapter;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class WebScraperService {
    @Autowired
    private StoryRepository storyRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    private static final String UPLOAD_DIR = "src/main/resources/static/images/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    @Async
    @Transactional
    public void scrapeStoryAsync(String targetUrl, String username) {
        User uploader = userRepository.findByUsername(username).orElse(null);
        if (uploader == null) {
            return;
        }
        String resultMsg = "";
        try {
            Document doc = Jsoup.connect(targetUrl)
                    .userAgent(USER_AGENT)
                    .timeout(15000)
                    .maxBodySize(0) 
                    .get();
            String title = doc.select("h3.title[itemprop='name']").text();
            if (title.isEmpty()) {
                resultMsg = "Lỗi: Không tìm thấy tiêu đề truyện. Link TruyenFull có hợp lệ không?: " + targetUrl;
                saveNotification(uploader, resultMsg);
                return;
            }
            String author = doc.select(".info a[itemprop='author']").text();
            String description = doc.select(".desc-text").html(); 
            String coverImageUrl = doc.select(".book img[itemprop='image']").attr("src");
            String savedImagePath = saveImageLocally(coverImageUrl);
            String slug = generateSlug(title);
            if (storyRepository.findBySlug(slug).isPresent()) {
                resultMsg = "Lỗi: Truyện '" + title + "' đã tồn tại trong hệ thống.";
                saveNotification(uploader, resultMsg);
                return;
            }
            Story newStory = Story.builder()
                    .title(title)
                    .author(author)
                    .description(description)
                    .coverImage(savedImagePath)
                    .slug(slug)
                    .status(Story.StoryStatus.ONGOING)
                    .uploader(uploader)
                    .views(0L)
                    .build();
            storyRepository.save(newStory);
            int chapterCount = scrapeChapters(newStory, doc, targetUrl);
            resultMsg = "Thành công: Đã cào xong truyện '" + title + "' với " + chapterCount + " chương.";
            saveNotification(uploader, resultMsg);
        } catch (IOException e) {
            e.printStackTrace();
            resultMsg = "Lỗi kết nối tới URL (" + targetUrl + "): " + e.getMessage();
            saveNotification(uploader, resultMsg);
        } catch (Exception e) {
            e.printStackTrace();
            resultMsg = "Lỗi hệ thống khi cào truyện (" + targetUrl + "): " + e.getMessage();
            saveNotification(uploader, resultMsg);
        }
    }
    private void saveNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .content(message)
                .type("SYSTEM")
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }
    private int scrapeChapters(Story story, Document firstPageDoc, String baseUrl) throws IOException {
        int totalSaved = 0;
        Double chapterNumber = 1.0;
        Document currentDoc = firstPageDoc;
        String currentUrl = baseUrl;
        while (true) {
            Elements chapterLinks = currentDoc.select("ul.list-chapter li a");
            for (Element link : chapterLinks) {
                String chapterUrl = link.attr("href");
                String chapterTitle = link.text();
                try {
                    Document chapterDoc = Jsoup.connect(chapterUrl)
                            .userAgent(USER_AGENT)
                            .timeout(15000)
                            .maxBodySize(0)
                            .get();
                    String contentHtml = chapterDoc.select(".chapter-c").html();
                    if (!contentHtml.isEmpty()) {
                        Chapter chapter = Chapter.builder()
                                .story(story)
                                .chapterNumber(chapterNumber)
                                .title(chapterTitle)
                                .content(contentHtml)
                                .type(Chapter.ChapterType.TEXT)
                                .build();
                        chapterRepository.save(chapter);
                        totalSaved++;
                        chapterNumber++;
                        Thread.sleep(1500);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to scrape chapter: " + chapterUrl);
                    e.printStackTrace();
                }
            }
            Element nextPageLink = currentDoc.select("ul.pagination li:not(.disabled) a").last();
            boolean hasNextPage = false;
            if (nextPageLink != null) {
                String nextUrl = nextPageLink.attr("href");
                String nextText = nextPageLink.text().toLowerCase();
                if (nextText.contains("tiếp") || nextPageLink.parent().hasClass("active") == false
                        && nextPageLink.parent().nextElementSibling() == null) {
                    currentUrl = nextUrl;
                    currentDoc = Jsoup.connect(currentUrl).userAgent(USER_AGENT).timeout(15000).maxBodySize(0).get();
                    hasNextPage = true;
                }
            }
            if (!hasNextPage) {
                break; 
            }
        }
        return totalSaved;
    }
    private String saveImageLocally(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "/images/default-cover.jpg";
        }
        try {
            URL url = new URL(imageUrl);
            String extension = imageUrl.substring(imageUrl.lastIndexOf("."));
            if (extension.contains("?")) {
                extension = extension.substring(0, extension.indexOf("?"));
            }
            if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                extension = ".jpg";
            }
            String fileName = UUID.randomUUID().toString() + extension;
            Path targetPath = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(targetPath.getParent());
            try (InputStream in = url.openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/images/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return "/images/default-cover.jpg"; 
        }
    }
    private String generateSlug(String input) {
        String slug = input.toLowerCase();
        slug = slug.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        slug = slug.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        slug = slug.replaceAll("[ìíịỉĩ]", "i");
        slug = slug.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        slug = slug.replaceAll("[ùúụủũưừứựửữ]", "u");
        slug = slug.replaceAll("[ỳýỵỷỹ]", "y");
        slug = slug.replaceAll("đ", "d");
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("^-|-$", "");
        return slug;
    }
}
