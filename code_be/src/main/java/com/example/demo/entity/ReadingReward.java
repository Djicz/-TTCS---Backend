package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reading_rewards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long chapterId;

    private Long storyId;

    private LocalDate rewardDate; // check 10 lần/ngày

    private LocalDateTime createdAt;
}