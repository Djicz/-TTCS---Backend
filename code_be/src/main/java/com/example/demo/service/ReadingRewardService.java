package com.example.demo.service;

import com.example.demo.entity.InventoryItem;
import com.example.demo.entity.ReadingReward;
import com.example.demo.entity.User;
import com.example.demo.repository.InventoryItemRepository;
import com.example.demo.repository.ReadingRewardRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingRewardService {

    private final ReadingRewardRepository rewardRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Transactional
    public String claimReward(User user, Long storyId, Long chapterId, int seconds) {

        // ❌ chưa đủ 60s
        if (seconds < 60) {
            throw new RuntimeException("Not enough reading time");
        }

        LocalDate today = LocalDate.now();

        // ❌ quá 10 lần/ngày
        int todayCount = rewardRepository.countByUserIdAndRewardDate(user.getId(), today);
        if (todayCount >= 10) {
            throw new RuntimeException("Daily reward limit reached");
        }

        // ❌ chapter đã nhận rồi
        boolean existed = rewardRepository.existsByUserIdAndChapterId(user.getId(), chapterId);
        if (existed) {
            throw new RuntimeException("Already claimed this chapter");
        }

        // ✅ lưu lịch sử
        ReadingReward reward = ReadingReward.builder()
                .userId(user.getId())
                .storyId(storyId)
                .chapterId(chapterId)
                .rewardDate(today)
                .createdAt(LocalDateTime.now())
                .build();

        rewardRepository.save(reward);

        // 🎁 cộng item
        InventoryItem item = inventoryItemRepository
                .findByUserIdAndItemType(user.getId(), "COIN")
                .orElseGet(() -> InventoryItem.builder()
                        .user(user)
                        .itemType("COIN")
                        .quantity(0)
                        .value(1)
                        .build());

        item.setQuantity(item.getQuantity() + 1);
        inventoryItemRepository.save(item);

        return "Reward claimed!";
    }
}