package com.example.demo.repository;

import com.example.demo.entity.ReadingReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReadingRewardRepository extends JpaRepository<ReadingReward, Long> {

    // check đã nhận reward chapter này chưa
    boolean existsByUserIdAndChapterId(Long userId, Long chapterId);

    // đếm số lần nhận trong ngày
    int countByUserIdAndRewardDate(Long userId, LocalDate date);
}