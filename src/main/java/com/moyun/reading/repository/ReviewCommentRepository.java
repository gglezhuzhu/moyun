package com.moyun.reading.repository;

import com.moyun.reading.entity.review.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 书评评论仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    /**
     * 查找指定书评的评论
     */
    Page<ReviewComment> findByReviewIdAndDeletedFalse(Long reviewId, Pageable pageable);

    /**
     * 统计指定书评的评论数量
     */
    long countByReviewIdAndDeletedFalse(Long reviewId);

    /**
     * 查找指定书评的评论（包含用户信息）
     */
    @Query(value = "SELECT rc FROM ReviewComment rc LEFT JOIN FETCH rc.user WHERE rc.review.id = :reviewId AND rc.deleted = false ORDER BY rc.createdAt ASC", countQuery = "SELECT COUNT(rc) FROM ReviewComment rc WHERE rc.review.id = :reviewId AND rc.deleted = false")
    Page<ReviewComment> findByReviewIdAndDeletedFalseWithUser(@Param("reviewId") Long reviewId, Pageable pageable);

    /**
     * 查找指定用户的评论
     */
    Page<ReviewComment> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    /**
     * 查找某个评论的子评论
     */
    Page<ReviewComment> findByParentIdAndDeletedFalse(Long parentId, Pageable pageable);

    /**
     * 查找顶级评论
     */
    Page<ReviewComment> findByReviewIdAndDeletedFalseAndParentIsNull(Long reviewId, Pageable pageable);
}