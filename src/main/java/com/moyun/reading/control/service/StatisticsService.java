package com.moyun.reading.control.service;

import com.moyun.reading.repository.BookRepository;
import com.moyun.reading.repository.ReviewRepository;
import com.moyun.reading.repository.UserRepository;
import com.moyun.reading.entity.review.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 获取平台统计数据
     */
    public Map<String, Long> getPlatformStatistics() {
        Map<String, Long> stats = new HashMap<>();

        // 获取真实的数据库统计
        stats.put("totalUsers", userRepository.count());
        stats.put("totalBooks", bookRepository.count());
        // 只统计已发布且未删除的书评
        stats.put("totalReviews", reviewRepository.countByDeletedFalseAndStatus(Review.ReviewStatus.PUBLISHED));

        // 讨论话题数量 - 由于没有讨论实体，暂时返回书评数的一半作为估算
        stats.put("totalDiscussions", reviewRepository.countByDeletedFalseAndStatus(Review.ReviewStatus.PUBLISHED) / 2);

        return stats;
    }

    /**
     * 格式化统计数字（显示为1,234格式）
     */
    public String formatNumber(Long number) {
        if (number == null) {
            return "0";
        }
        if (number < 1000) {
            return number.toString();
        } else if (number < 10000) {
            return String.format("%,d", number);
        } else {
            return String.format("%.1fK", number / 1000.0);
        }
    }
}