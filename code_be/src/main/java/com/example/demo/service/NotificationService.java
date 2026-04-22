package com.example.demo.service;

import com.example.demo.entity.Bookshelf;
import com.example.demo.entity.Chapter;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.BookshelfRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;

    /**
     * Gửi thông báo đến tất cả người dùng đã lưu truyện (có bật notify)
     * khi có chương mới được đăng.
     */
    @Transactional
    public void notifyNewChapter(Chapter chapter) {
        Story story = chapter.getStory();
        List<Bookshelf> subscribers = bookshelfRepository.findByStoryIdAndNotifyOnNewChapterTrue(story.getId());

        for (Bookshelf bs : subscribers) {
            User user = bs.getUser();
            Notification notification = Notification.builder()
                    .user(user)
                    .content("【" + story.getTitle() + "】 vừa ra chương mới: Chương " + chapter.getChapterNumber()
                            + (chapter.getTitle() != null && !chapter.getTitle().isBlank()
                                    ? " - " + chapter.getTitle()
                                    : ""))
                    .type("NEW_CHAPTER")
                    .url("/reader/" + chapter.getId())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }
    }

    /** Lấy tất cả thông báo của user, mới nhất trước */
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Đếm số thông báo chưa đọc */
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /** Đánh dấu một thông báo là đã đọc */
    @Transactional
    public void markRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(userId)) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
    }

    /** Đánh dấu tất cả thông báo của user là đã đọc */
    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    /** Thông báo cho chủ truyện khi có bình luận mới */
    @Transactional
    public void notifyNewComment(com.example.demo.entity.Comment comment) {
        Story story = comment.getStory();
        User uploader = story.getUploader();
        if (uploader == null || uploader.getId().equals(comment.getUser().getId())) return;

        Notification notification = Notification.builder()
                .user(uploader)
                .content("User @" + comment.getUser().getUsername() + " vừa bình luận trong truyện 【" + story.getTitle() + "】")
                .type("NEW_COMMENT")
                .url("/story/" + story.getSlug() + "#comment-" + comment.getId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    /** Thông báo khi có tin nhắn chat riêng mới */
    @Transactional
    public void notifyNewPrivateChat(User sender, User recipient) {
        if (sender == null || recipient == null || sender.getId().equals(recipient.getId())) return;

        Notification notification = Notification.builder()
                .user(recipient)
                .content("Bạn có tin nhắn mới từ @" + sender.getUsername())
                .type("PRIVATE_CHAT")
                .url("/?chatWith=" + sender.getId() + "&chatUser=" + sender.getUsername())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }
}
