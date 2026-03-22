 package com.example.demo.controller;
 import com.example.demo.service.WebScraperService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.annotation.AuthenticationPrincipal;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.PathVariable;
 import com.example.demo.entity.Story;
 import com.example.demo.service.StoryService;
 import java.util.List;
 @Controller
 public class ScraperController {
     @Autowired
     private WebScraperService webScraperService;
     @Autowired
     private StoryService storyService;
     @GetMapping("/admin/scraper")
     @PreAuthorize("hasRole('ADMIN')")
     public String getScraperPage(Model model) {
         List<Story> stories = storyService.getRecentlyUpdatedStories(100);
         model.addAttribute("stories", stories);
         return "admin/scraper";
     }
     @PostMapping("/admin/scraper/run")
     @PreAuthorize("hasRole('ADMIN')")
     public String runScraper(@RequestParam("urls") String targetUrls,
             @AuthenticationPrincipal UserDetails userDetails,
             Model model) {
         if (targetUrls == null || targetUrls.trim().isEmpty()) {
             model.addAttribute("error", "Vui lòng nhập ít nhất 1 link hợp lệ.");
             model.addAttribute("stories", storyService.getRecentlyUpdatedStories(100));
             return "admin/scraper";
         }
         String[] urls = targetUrls.split("\\r?\\n");
         StringBuilder errorMsg = new StringBuilder();
         int successCount = 0;
         for (String url : urls) {
             String trimmedUrl = url.trim();
             if (trimmedUrl.isEmpty())
                 continue;
             if (!trimmedUrl.contains("truyenfull")) {
                 errorMsg.append("Link không hợp lệ (Bỏ qua): ").append(trimmedUrl).append("<br/>");
                 continue;
             }
             try {
                 webScraperService.scrapeStoryAsync(trimmedUrl, userDetails.getUsername());
                 successCount++;
             } catch (Exception e) {
                 errorMsg.append("Lỗi khởi tạo tải ").append(trimmedUrl).append(": ").append(e.getMessage())
                         .append("<br/>");
             }
         }
         if (successCount > 0) {
             model.addAttribute("success",
                     "Đã gửi yêu cầu cào dữ liệu cho " + successCount
                             + " truyện. <b>Quá trình đang chạy ngầm</b>. Bạn sẽ nhận được thông báo ở góc phải màn hình khi hoàn tất từng truyện!");
         }
         if (errorMsg.length() > 0) {
             model.addAttribute("error", "Có lỗi xảy ra với một số link:<br/>" + errorMsg.toString());
         }
         model.addAttribute("stories", storyService.getRecentlyUpdatedStories(100));
         return "admin/scraper";
     }
     @PostMapping("/admin/story/delete/{id}")
     @PreAuthorize("hasRole('ADMIN')")
     public String deleteStory(@PathVariable Long id) {
         storyService.deleteStory(id);
         return "redirect:/admin/scraper";
     }
 }
