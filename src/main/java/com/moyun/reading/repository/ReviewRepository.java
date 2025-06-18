package com.moyun.reading.repository;

import com.moyun.reading.entity.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 书评仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 查找指定状态的书评
     */
    Page<Review> findByDeletedFalseAndStatus(Review.ReviewStatus status, Pageable pageable);

    /**
     * 查找某个图书的书评
     */
    Page<Review> findByBookIdAndDeletedFalseAndStatus(Long bookId, Review.ReviewStatus status, Pageable pageable);

    /**
     * 查找某个用户的书评
     */
    Page<Review> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    /**
     * 统计已发布的书评数量
     */
    long countByDeletedFalseAndStatus(Review.ReviewStatus status);
}