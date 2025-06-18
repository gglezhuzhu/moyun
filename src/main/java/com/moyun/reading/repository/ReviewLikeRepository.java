package com.moyun.reading.repository;

import com.moyun.reading.entity.review.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 书评点赞仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    /**
     * 查找用户对书评的点赞记录
     */
    Optional<ReviewLike> findByReviewIdAndUserIdAndDeletedFalse(Long reviewId, Long userId);

    /**
     * 检查用户是否已点赞书评
     */
    boolean existsByReviewIdAndUserIdAndDeletedFalse(Long reviewId, Long userId);

    /**
     * 统计书评的点赞数
     */
    long countByReviewIdAndDeletedFalse(Long reviewId);
}