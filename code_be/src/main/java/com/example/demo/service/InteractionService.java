// package com.example.demo.service;
// import com.example.demo.entity.InventoryItem;
// import com.example.demo.entity.Story;
// import com.example.demo.entity.User;
// import com.example.demo.repository.InventoryItemRepository;
// import com.example.demo.repository.StoryRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// @Service
// @RequiredArgsConstructor
// public class InteractionService {
//     private final InventoryItemRepository inventoryItemRepository;
//     private final StoryRepository storyRepository;
//     @Transactional
//     public void pushStory(User user, Long storyId, int pushCount) {
//         InventoryItem item = inventoryItemRepository.findByUserIdAndItemType(user.getId(), "PUSH_TICKET")
//                 .orElseThrow(() -> new RuntimeException("You do not have any push tickets."));
//         if (item.getQuantity() < pushCount) {
//             throw new RuntimeException("Not enough push tickets.");
//         }
//         item.setQuantity(item.getQuantity() - pushCount);
//         inventoryItemRepository.save(item);
//         Story story = storyRepository.findById(storyId).orElseThrow();
//         story.setViews(story.getViews() + (pushCount * 10L));
//         storyRepository.save(story);
//     }
// }
